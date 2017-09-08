package me.daei.soundmeter;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    float volume = 10000;
    private MyMediaRecorder mRecorder;
    private static final int msgWhat = 0x1001;
    private static final int refreshTime = 300;//更新时间，会与静态眨眼时间冲突，太大的话会反应迟钝
    private ImageView imageView;
    AnimationDrawable  ani;
    private int i = 0;//控制眨眼的时间频率，每循环10次眨眼一次

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setBackgroundResource(R.drawable.ani);
        ani = (AnimationDrawable) imageView.getBackground();
        mRecorder = new MyMediaRecorder();
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.hasMessages(msgWhat)) {
                return;
            }
            volume = mRecorder.getMaxAmplitude();  //获取声压值
            if(volume > 0 && volume < 1000000) {


                World.setDbCount(20 * (float)(Math.log10(volume)));  //将声压值转为分贝值

                System.out.println("xxxxxxxxxxxxxx: " + volume);
                ani.selectDrawable(0);//设置动画结束帧恢复到第一帧
                ani.stop();
                if (volume >10000) {//声音大于一定值，执行说话动画
                    imageView.setBackgroundResource(R.drawable.anj);
                    ani = (AnimationDrawable) imageView.getBackground();
                    ani.start();
                }
                else {
                    i++;
                    if(i %10 == 0 )//控制眨眼的时间频率，每循环10次眨眼一次
                    {
                        i = 0;
                        ani.stop();
                        imageView.setBackgroundResource(R.drawable.ani);
                        ani = (AnimationDrawable) imageView.getBackground();
                        ani.start();
                     }
                }

            }


            handler.sendEmptyMessageDelayed(msgWhat, refreshTime);//更新UI时间延迟要大于动画时间，否则动画会被截断，后面的显示不出来
        }
    };

    private void startListenAudio() {
        handler.sendEmptyMessageDelayed(msgWhat, refreshTime);
    }

    /**
     * 开始记录
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }else{
                Toast.makeText(this, "启动录音失败", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, "录音机已被占用或录音权限被禁止", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        File file = FileUtil.createFile("temp.amr");
        if (file != null) {
            Log.v("file", "file =" + file.getAbsolutePath());
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), "创建文件失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 停止记录
     */
    @Override
    protected void onPause() {
        super.onPause();
        mRecorder.delete(); //停止记录并删除录音文件
        handler.removeMessages(msgWhat);
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(msgWhat);
        mRecorder.delete();
        super.onDestroy();
    }
}
