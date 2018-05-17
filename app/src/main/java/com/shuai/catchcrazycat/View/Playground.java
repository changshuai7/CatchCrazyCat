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
         * abstract  void addCallback（SurfaceHolder.Callback callback );为SurfaceHolder添加一个SurfaceHolder.Callback回调接口。
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
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }
        //将所有的点设置状态：空心
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS.STATUS_OFF);
            }
        }
        //设置猫的位置。中间位置。
        cat = new Dot(COL / 2 - 1, ROW / 2 - 1);
        //设置猫的状态：猫踩到。
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS.STATUS_IN);
        //随机设置BOLCKS个实心位置。
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
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {


                DISTANCE = 0;
                if (i % 2 != 0) {
                    DISTANCE = WIDTH / 2;
                }

                //获取到点
                Dot dot = getDot(j, i);

                //得到点的状态
                switch (dot.getStatus()) {
                    case STATUS_IN://神经猫所在的点
                        paint.setColor(0XFFEEEEEE);//设置颜色白色
                        break;
                    case STATUS_ON://实心
                        paint.setColor(0XFFFFAA00);//设置实心颜色
                        break;
                    case STATUS_OFF://空心
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
                        dot.getX() * WIDTH + DISTANCE+ length,
                        dot.getY() * WIDTH + OFFSET,
                        (dot.getX() + 1)* WIDTH + DISTANCE + length,
                        (dot.getY() + 1) * WIDTH+ OFFSET),
                        paint
                );


            }
        }
        int left;
        int top;
        if (cat.getY() % 2 == 0) {//如果是偶数行
            left = cat.getX() * WIDTH;
            top = cat.getY() * WIDTH;
        } else {//如果是奇数行
            left = (WIDTH / 2) + cat.getX() * WIDTH;
            top = cat.getY() * WIDTH;
        }
        // 此处神经猫图片的位置是根据效果图来调整的
        cat_drawable.setBounds(left - WIDTH / 6 + length, top - WIDTH / 2
                + OFFSET, left + WIDTH + length, top + WIDTH + OFFSET);
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
        //Surface创建时激发
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();//绘图
            startTimer();//启动定时器
        }

        //Surface发生改变时调用。
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            WIDTH = width / (COL + 1);
            OFFSET = height - WIDTH * ROW - 2 * WIDTH;
            length = WIDTH / 3;
            SCREEN_WIDTH = width;
        }

        //Surface销毁时调用。
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopTimer();
        }
    };

    // 开启定时任务
    private void startTimer() {
        timer = new Timer();
        timerttask = new TimerTask() {
            public void run() {
                gifImage();
            }
        };
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
        if (dot.getX() * dot.getY() == 0 || dot.getX() + 1 == COL
                || dot.getY() + 1 == ROW) {
            return true;
        }
        return false;
    }

    // 移动cat至指定点
    private void moveTo(Dot dot) {
        dot.setStatus(Dot.STATUS.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS.STATUS_OFF);
        cat.setXY(dot.getX(), dot.getY());
    }

    // 获取one在方向dir上的可移动距离
    private int getDistance(Dot one, int dir) {
        int distance = 0;
        if (inEdge(one)) {
            return 1;
        }
        Dot ori = one;
        Dot next;
        while (true) {
            next = getNeighbour(ori, dir);
            if (next.getStatus() == Dot.STATUS.STATUS_ON) {
                return distance * -1;
            }
            if (inEdge(next)) {
                distance++;
                return distance;
            }
            distance++;
            ori = next;
        }
    }

    // 获取dot的相邻点，返回其对象
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

    // cat的移动算法
    private void move() {
        if (inEdge(cat)) {
            failure();
            return;
        }
        Vector<Dot> available = new Vector<>();
        Vector<Dot> direct = new Vector<>();
        HashMap<Dot, Integer> hash = new HashMap<>();
        for (int i = 1; i < 7; i++) {
            Dot n = getNeighbour(cat, i);
            if (n.getStatus() == Dot.STATUS.STATUS_OFF) {
                available.add(n);
                hash.put(n, i);
                if (getDistance(n, i) > 0) {
                    direct.add(n);
                }
            }
        }

        //可用通道为0，则成功
        if (available.size() == 0) {
            win();
            canMove = false;
        }
        //可用通道为1，则移动到这个通道
        else if (available.size() == 1) {
            moveTo(available.get(0));
        }

        //其他情况
        else {
            Dot best = null;
            if (direct.size() != 0) {
                int min = 20;
                for (int i = 0; i < direct.size(); i++) {
                    if (inEdge(direct.get(i))) {
                        best = direct.get(i);
                        break;
                    } else {
                        int t = getDistance(direct.get(i),
                                hash.get(direct.get(i)));
                        if (t < min) {
                            min = t;
                            best = direct.get(i);
                        }
                    }
                }
            } else {
                int max = 1;
                for (int i = 0; i < available.size(); i++) {
                    int k = getDistance(available.get(i),
                            hash.get(available.get(i)));
                    if (k < max) {
                        max = k;
                        best = available.get(i);
                    }
                }
            }
            moveTo(best);
        }
        if (inEdge(cat)) {
            failure();
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
