package com.triosoft.curtainview;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View movingView = findViewById(R.id.fl);
        View container = findViewById(R.id.container);
        movingView.post(() -> movingView.setY(container.getTop() - movingView.getHeight()));
        container.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            boolean isEvent = false;
            boolean topToBottom;
            private int offset = 250;
            private float topTouchPosition, bottomTouchPosition;

            private void calculatePositions(MotionEvent event) {
                dX = container.getX() - event.getRawX();
                dY = container.getY() - event.getRawY();
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (movingView.getY() == movingView.getHeight() * -1 && event.getY() < container.getTop() + offset) {
                            topToBottom = true;
                            topTouchPosition = event.getY();
                            calculatePositions(event);
                            isEvent = true;
                        } else if (container.getBottom() == movingView.getY() + movingView.getHeight() && event.getY() > container.getBottom() - offset) {
                            topToBottom = false;
                            bottomTouchPosition = event.getY();
                            calculatePositions(event);
                            isEvent = true;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (isEvent) {
                            movingView.animate()
//                                .x(event.getRawX() + dX)
                                    .y(topToBottom ? event.getY() - topTouchPosition - movingView.getHeight() : -movingView.getHeight() + event.getY() + (container.getBottom() - bottomTouchPosition))
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isEvent) {
                            if (container.getBottom() / 3f < event.getY()) {
                                movingView.animate()
                                        .y(container.getBottom() - movingView.getHeight())
                                        .setDuration(300)
                                        .start();
                            } else {
                                movingView.animate()
                                        .y(container.getTop() - movingView.getHeight())
                                        .setDuration(300)
                                        .start();
                            }
                        }
                        isEvent = false;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

    }
}
