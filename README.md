# Curtain view UI component for android applications
A curtain view library to use in your Android applications, its movement behavior is similar to some extent to the android top down system curtain.

(GIF demo)
<img src="https://github.com/tarelendil/curtainview/blob/master/curtain_gif.gif" width="270" height="480">

<h3><b>Gradle configurations</b></h3>
<p>Add: 
<code>maven { url "https://jitpack.io" } </code>
<br/>to:
   <code> allprojects {
            repositories {    
                maven { url "https://jitpack.io" }
            }
       }
   </code><br>
Add:
    <code> implementation 'com.github.tarelendil:curtainview:0.1.1' </code>
    to your application dependecies list
    </p>
 <h3><b>Usage</b></h3>
 Please take a look at the sample app.
 
 The view is defined in the xml layout file.
  <br>Example:<br>
  
  ```
<com.stas.android.curtainview.views.curtain.CurtainContainerView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000"
    app:ccv_action_bar="@+id/tv_fake_action_bar"
    app:ccv_curtain_view="@+id/curtain"
    app:ccv_alpha_animation_duration_millis="400"
    app:ccv_velocity_minimum_threshold="1300"
    tools:context=".activities.TestActivity">
    
        <TextView
            android:id="@+id/tv_fake_action_bar"
            android:layout_width="match_parent"
            android:layout_height="70dp"
            app:layout_constraintTop_toTopOf="parent"
            android:background="#836666"
            android:textSize="25sp"
            android:textColor="#000"
            android:gravity="center"
            android:text="Fake Action Bar"/>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:gravity="center_horizontal"
            android:orientation="vertical"
            app:layout_constraintTop_toBottomOf="@+id/tv_fake_action_bar">
         <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Button1" />
        </LinearLayout>
        <LinearLayout
            android:id="@+id/curtain"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="#fff"
            android:gravity="center_horizontal"
            android:orientation="vertical"
            android:visibility="invisible"
            app:layout_constraintTop_toTopOf="parent">
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Button1" />
            <Button
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Button2" />
        </LinearLayout>    
</com.stas.android.curtainview.views.curtain.CurtainContainerView>

```
**CurtainContainerView is a contraintLayout**, so take a notice and use accordingly.
<p> Must provide the id of the curtain view that you want to use, it can be any view, layout that you want: <code> app:ccv_curtain_view="@+id/curtain"</code><br>
 The other attributes are optional:<br>
 ccv_action_bar optional top action bar<br>
 ccv_alpha_animation_duration_millis is optional and will be used for the alpha animation on the action bar if you are using one. If you  did provide an action bar but not the animation duration attribute, the default duration will be used.<br>
 ccv_velocity_minimum_threshold optional: if the velocity is reached then the curtain will move to the bottom or to the top according to the direction of the users touch on the screen.
  </p>
  
  ```
  Copyright 2020 Stas Kranzov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software hi
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
