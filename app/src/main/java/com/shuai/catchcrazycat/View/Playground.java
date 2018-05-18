package com.shuai.catchcrazycat.View;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.shuai.catchcrazycat.R;

/**
 * 游戏View
 */

public class Playground extends SurfaceView implements OnTouchListener {

    // 行数
    private static final int ROW = 9;
    // 列数
    private static final int COL = 9;
    // 障碍的数量
    private static final int BOCKS = COL * ROW / 5;

    // 屏幕宽度
    private int SCREEN_WIDTH;
    // 每个通道的宽度
    private int WIDTH;


    // 奇数行和偶数行通道间的位置偏差量
    //i % 2 != 0 需要有distance,   i % 2  = 0 不需要有distance
    private int DISTANCE;


    // 屏幕顶端和通道最顶端间的距离
    private int OFFSET;
    // 整个通道与屏幕两端间的距离
    private int length;

    // 做成神经猫动态图效果的单张图片
    private Drawable cat_drawable;
    // 背景图
    private Drawable background;

    // 神经猫动态图的索引
    private int index = 0;

    // 总的通道
    private Dot[][] matrix;//这里相当于路径点的二维集合

    //神经猫所在地
    private Dot cat;

    private Timer timer;

    private TimerTask timerttask;

    private Context context;

    //行走的步数
    private int steps;

    private boolean canMove = true;

    private int[] images = {R.drawable.cat1, R.drawable.cat2, R.drawable.cat3,
            R.drawable.cat4, R.drawable.cat5, R.drawable.cat6, R.drawable.cat7,
            R.drawable.cat8, R.drawable.cat9, R.drawable.cat10,
            R.drawable.cat11, R.drawable.cat12, R.drawable.cat13,
            R.drawable.cat14, R.drawable.cat15, R.drawable.cat16};

    public Playground(Context context) {
        super(context);
        matrix = new Dot[ROW][COL];

        if (Build.VERSION.SDK_INT < 21) {
            cat_drawable = getResources().getDrawable(images[index]);
            background = getResources().getDrawable(R.drawable.bg);
        } else {
            cat_drawable = getResources().getDrawable(images[index], null);
            background = getResources().getDrawable(R.drawable.bg, null);
        }
        this.context = context;


        /**
         * 初始化游戏数据
         */
        initGame();


        /**
         *
         *
         * SurfaceView中调用getHolder方法，可以获得当前SurfaceView中的surface对应的SurfaceHolder，SurfaceHolder中重要的方法有
         *
         * abstract void addCallback（SurfaceHolder.Callback callback );为SurfaceHolder添加一个SurfaceHolder.Callback回调接口。
         *
         */
        getHolder().addCallback(callback);


        /**
         *
         * Playgound实现了OnTouchListener接口，所以可以监听手指的触摸事件
         *
         */
        setOnTouchListener(this);


        /**
         * setFocusable 使能控件获得焦点，设置为true时，并不是说立刻获得焦点，要想立刻获得焦点，得用requestFocus；
           使能获得焦点，就是说具备获得焦点的机会、能力，当有焦点在控件之间移动时，控件就有这个机会、能力得到焦点。
         */
        this.setFocusable(true);


        /**
         * 在进入触摸输入模式后,该控件是否还有获得焦点的能力.
         */
        this.setFocusableInTouchMode(true);
    }

    // 初始化游戏
    private void initGame() {
        //初始化步骤为0
        steps = 0;

        //for循环，将所有的点添加到二维数组中。
        //(0,0),(1,0),(2,0),(3,0),(4,0),(5,0),(6,0),(7,0),(8,0)
        //(0,1),(1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1)
        //(0,2),(1,1),(2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(8,2)
        //(0,3),(1,3),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3)
        //(0,4),(1,4),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(8,4)
        //(0,5),(1,5),(2,5),(3,5),(4,5),(5,5),(6,5),(7,5),(8,5)
        //(0,6),(1,6),(2,6),(3,6),(4,6),(5,6),(6,6),(7,6),(8,6)
        //(0,7),(1,7),(2,7),(3,7),(4,7),(5,7),(6,7),(7,7),(8,7)
        //(0,8),(1,8),(2,8),(3,8),(4,8),(5,8),(6,8),(7,8),(8,8)

        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }

        //将所有的点设置状态：路障关
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS.STATUS_OFF);
            }
        }

        //设置猫的位置。中间位置。
        cat = new Dot(COL / 2 - 1, ROW / 2 - 1);

        //设置猫的状态：神经猫所在位置
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS.STATUS_IN);

        //随机设置路障的位置。
        for (int i = 0; i < BOCKS; ) {
            //Math类的random()方法可以生成大于等于0.0、小于1.0的double型随机数
            int x = (int) ((Math.random() * 100) % COL);//%求余（求模）运算
            int y = (int) ((Math.random() * 100) % ROW);
            if (getDot(x, y).getStatus() == Dot.STATUS.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS.STATUS_ON);
                i++;
            }
        }
    }

    // 绘图

    /**
     * 重新绘制SurfaceView
     */
    private void redraw() {

        //得到绘制View的对象。
        Canvas canvas = getHolder().lockCanvas();
        //绘制颜色
        canvas.drawColor(Color.rgb(0, 0x8c, 0xd7));
        //创建画笔
        Paint paint = new Paint();
        //设置画笔flags
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        //遍历所有的点
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {


                DISTANCE = 0;
                if (i % 2 != 0) {//如果是偶数行
                    DISTANCE = WIDTH / 2; //如果是偶数行，那么存在位置偏差
                }

                //根据坐标，获取到点
                Dot dot = getDot(j, i);

                //得到点的状态
                switch (dot.getStatus()) {
                    case STATUS_IN://神经猫所在的点
                        paint.setColor(0XFFEEEEEE);//设置颜色白色
                        break;
                    case STATUS_ON://路障开
                        paint.setColor(0XFFFFAA00);//设置实心颜色
                        break;
                    case STATUS_OFF://路障关
                        paint.setColor(0X74000000);//设置空心颜色
                        break;
                    default:
                        break;
                }


                /**
                 *
                 * drawOval:画一个椭圆。在点的我位置上画一个椭圆。
                 * canvas.drawOval(rectF,paint_red);
                 *
                 */

                canvas.drawOval(new RectF(
                        dot.getX() * WIDTH + DISTANCE+ length,//路障左侧距离和屏幕左边距离：路障个数*路障宽度+左侧差距离+通道和两边的距离
                        dot.getY() * WIDTH + OFFSET,//路障上侧和屏幕上侧距离：路障个数*路障宽度+和屏幕顶端距离
                        (dot.getX() + 1)* WIDTH + DISTANCE + length,//路障右侧和屏幕左侧距离：（路障个数+1）*路障宽度+左侧差距离+通道和两边的距离
                        (dot.getY() + 1) * WIDTH+ OFFSET),//路障下侧和屏幕下侧距离：（路障个数+1）*路障宽度+和屏幕顶端距离
                        paint //画笔
                );


            }
        }

        //定义两个int参数：神经猫（左侧的点）和左侧屏幕的距离、和上侧屏幕的距离
        int left;
        int top;
        if (cat.getY() % 2 == 0) {//如果是奇数行，不存在位置偏差
            left = cat.getX() * WIDTH;
            top = cat.getY() * WIDTH;
        } else {//如果是偶数航，是存在偏差的
            left = (WIDTH / 2) + cat.getX() * WIDTH;
            top = cat.getY() * WIDTH;
        }
        // 此处神经猫图片的位置是根据效果图来调整的。这个参数没有固定的实际意义。
        cat_drawable.setBounds(
                left - WIDTH / 6 + length,
                top - WIDTH / 2+ OFFSET,
                left + WIDTH + length,
                top + WIDTH + OFFSET);


        cat_drawable.draw(canvas);
        background.setBounds(0, 0, SCREEN_WIDTH, OFFSET);
        background.draw(canvas);
        getHolder().unlockCanvasAndPost(canvas);
    }

    /**
     * SurfaceHolder.Callback是SurfaceHolder接口内部的静态子接口，SurfaceHolder.Callback中定义了三个接口方法：
     * 1： public void sufaceChanged(SurfaceHolder holder,int format,int width,int height){}//Surface的大小发生改变时调用。
     * 2： public void surfaceCreated(SurfaceHolder holder){}//Surface创建时激发，一般在这里调用画面的线程。
     * 3： public void surfaceDestroyed(SurfaceHolder holder){}//销毁时激发，一般在这里将画面的线程停止、释放。
     */
    Callback callback = new Callback() {
        //Surface创建时激发，一般在这里调用画面的线程。
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();//绘图：绘制屏幕上所有图形。
            startTimer();//启动定时器：主要是启动神经猫的动画。
        }

        //Surface的大小发生改变时调用。//todo
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            WIDTH = width / (COL + 1);
            OFFSET = height - WIDTH * ROW - 2 * WIDTH;
            length = WIDTH / 3;
            SCREEN_WIDTH = width;
        }

        //销毁时激发，一般在这里将画面的线程停止、释放。
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopTimer();
        }
    };

    // 开启定时任务，切换图片，其实这种切换效率是很低的。
    private void startTimer() {
        timer = new Timer();
        timerttask = new TimerTask() {
            public void run() {
                gifImage();
            }
        };
        /**
         * delay50表示一张图片的延迟时间：period65表示持续时间。
         */
        timer.schedule(timerttask, 50, 65);
    }

    // 停止定时任务
    public void stopTimer() {
        timer.cancel();
        timer.purge();
    }

    // 动态图。神经猫的动态图。
    private void gifImage() {
        index++;
        if (index > images.length - 1) {
            index = 0;
        }
        if (Build.VERSION.SDK_INT < 21) {
            cat_drawable = getResources().getDrawable(images[index]);
        } else {
            cat_drawable = getResources().getDrawable(images[index], null);
        }
        redraw();
    }

    // 获取通道对象
    private Dot getDot(int x, int y) {
        return matrix[y][x];
    }

    // 判断神经猫是否处于边界
    private boolean inEdge(Dot dot) {
        if (dot.getX() * dot.getY() == 0 //上左边缘
                || dot.getX() + 1 == COL //右边缘
                || dot.getY() + 1 == ROW //下边缘
                ) {
            return true;//是
        }
        return false;//否
    }

    // 移动cat至指定Dot点
    private void moveTo(Dot dot) {
        dot.setStatus(Dot.STATUS.STATUS_IN);//设置神经猫的新状态为：神经猫的位置
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS.STATUS_OFF);//将原神经猫的位置设置为非路障
        cat.setXY(dot.getX(), dot.getY());//设置神经猫的新状态。
    }

    // 获取one这个点在方向dir上的可移动距离（dir总共有6个方向）
    private int getDistance(Dot one, int dir) {//todo
        int distance = 0;//默认可移动距离为0。初始值。
        if (inEdge(one)) {//如果是在边界。则返回1，即可移动
            return 1;
        }
        Dot ori = one;//初始点
        Dot next;//下一步移动点

        //循环移动点，计算可以移动多少距离
        while (true) {
            next = getNeighbour(ori, dir);//获取dir方向相邻的点
            //如果这个相邻的点是路障
            if (next.getStatus() == Dot.STATUS.STATUS_ON) {//路障开
                return distance * -1;
            }
            //如果这个相邻的点是边界
            if (inEdge(next)) {
                distance++;
                return distance;
            }
            //其他
            distance++;
            ori = next;
        }
    }

    // 获取dot的相邻点，返回其对象。总共6个点
    private Dot getNeighbour(Dot dot, int dir) {
        switch (dir) {
            case 1:
                return getDot(dot.getX() - 1, dot.getY());
            case 2:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX() - 1, dot.getY() - 1);
                } else {
                    return getDot(dot.getX(), dot.getY() - 1);
                }
            case 3:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX(), dot.getY() - 1);
                } else {
                    return getDot(dot.getX() + 1, dot.getY() - 1);
                }
            case 4:
                return getDot(dot.getX() + 1, dot.getY());
            case 5:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX(), dot.getY() + 1);
                } else {
                    return getDot(dot.getX() + 1, dot.getY() + 1);
                }
            case 6:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX() - 1, dot.getY() + 1);
                } else {
                    return getDot(dot.getX(), dot.getY() + 1);
                }
        }
        return null;
    }

    // cat的移动算法（六边形）只移动一步。移动到最优路径。
    private void move() {
        //如果cat在边界，则失败。return
        if (inEdge(cat)) {
            failure();
            return;
        }

        Vector<Dot> available = new Vector<>();//6个点中，非路障的点 的集合
        Vector<Dot> direct = new Vector<>();//6个点中，在某个方向可移动距离大于0 的集合。
        HashMap<Dot, Integer> hash = new HashMap<>();//6个点中，非路障的(点，方向)。集合存放到HashMap中。
        for (int i = 1; i < 7; i++) {//cat在1-6这6个方向上 遍历
            Dot n = getNeighbour(cat, i);//获取某个方向上相邻的点。
            if (n.getStatus() == Dot.STATUS.STATUS_OFF) {//如果路障关
                available.add(n);//将此点加入available集合中。
                hash.put(n, i);//将(点，方向)。集合存放到HashMap中。
                if (getDistance(n, i) > 0) {//如果这个点在某个方向上可移动距离大于0，则将点存放于direct中。
                    direct.add(n);
                }
            }
        }

        //可用通道为0，则成功
        if (available.size() == 0) {
            win();
            canMove = false;//不可移动
        }
        //可用通道为1，则移动到这个通道
        else if (available.size() == 1) {
            moveTo(available.get(0));//就一个通道，集合中就一个元素，必然是get(0)
        }

        //其他情况
        else {
            Dot best = null;//最优路径
            if (direct.size() != 0) {//6个点中，某个点的可移动距离不等于0的时候
                int min = 20;
                for (int i = 0; i < direct.size(); i++) {//遍历direct的所有的这些点。
                    if (inEdge(direct.get(i))) {//如果点是边界
                        best = direct.get(i);//那么这个就是最优路径
                        break;
                    } else {
                        int t = getDistance(direct.get(i),hash.get(direct.get(i)));//获取这个点在这个方向上的距离
                        if (t < min) {
                            min = t;
                            best = direct.get(i);
                        }
                    }
                }
            } else {//如果6个点中，direct.size() = 0 6个点中，在某个方向可移动距离没有大于0的。
                int max = 1;//则仅可以移动一步。
                for (int i = 0; i < available.size(); i++) {//遍历可移动的点。
                    int k = getDistance(available.get(i), hash.get(available.get(i)));//获取可移动的距离
                    if (k < max) {
                        max = k;
                        best = available.get(i);
                    }
                }
            }
            moveTo(best);//移动到最优路径。
        }
        if (inEdge(cat)) {
            failure();//到达边界，则失败。
        }
    }

    // 通关失败
    private void failure() {
        Builder dialog = new Builder(context);
        dialog.setTitle("通关失败");
        dialog.setMessage("你让神经猫逃走了！");
        dialog.setCancelable(false);
        dialog.setNegativeButton("再玩一次", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                initGame();
                canMove = true;
            }
        });
        dialog.setPositiveButton("取消", null);
        dialog.show();
    }

    // 通关成功
    private void win() {
        Builder dialog = new Builder(context);
        dialog.setTitle("通关成功");
        dialog.setMessage("你用" + (steps + 1) + "步捕捉到了神经猫！");
        dialog.setCancelable(false);
        dialog.setNegativeButton("再玩一次", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                initGame();
                canMove = true;
            }
        });
        dialog.setPositiveButton("取消", null);
        dialog.show();
    }

    // 触屏事件
    public boolean onTouch(View v, MotionEvent event) {

        int x, y;
        if (event.getAction() == MotionEvent.ACTION_UP) {//抬起手指
            if (event.getY() <= OFFSET) {
                return true;
            }
            y = (int) ((event.getY() - OFFSET) / WIDTH);
            if (y % 2 == 0) {
                if (event.getX() <= length
                        || event.getX() >= length + WIDTH * COL) {
                    return true;
                }
                x = (int) ((event.getX() - length) / WIDTH);
            } else {
                if (event.getX() <= (length + WIDTH / 2)
                        || event.getX() > (length + WIDTH / 2 + WIDTH * COL)) {
                    return true;
                }
                x = (int) ((event.getX() - WIDTH / 2 - length) / WIDTH);
            }
            if (x + 1 > COL || y + 1 > ROW) {
                return true;
            } else if (inEdge(cat) || !canMove) {
                initGame();
                canMove = true;
                return true;
            } else if (getDot(x, y).getStatus() == Dot.STATUS.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS.STATUS_ON);
                move();
                steps++;
            }
        }
        return true;
    }

    // 按键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopTimer();
        }
        return super.onKeyDown(keyCode, event);
    }

}
