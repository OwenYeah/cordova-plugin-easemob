/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bjzjns.hxplugin.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseBaseActivity;

public class ContextMenuActivity extends EaseBaseActivity {
    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int RESULT_CODE_FORWARD = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EMMessage message = getIntent().getParcelableExtra("message");

        int type = message.getType().ordinal();
        if (type == EMMessage.Type.TXT.ordinal()) {
            if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                setContentView(getResources().getIdentifier("em_context_menu_for_image", "layout", getPackageName()));
            } else {
                setContentView(getResources().getIdentifier("em_context_menu_for_text", "layout", getPackageName()));
            }
        } else if (type == EMMessage.Type.LOCATION.ordinal()) {
            setContentView(getResources().getIdentifier("em_context_menu_for_location", "layout", getPackageName()));
        } else if (type == EMMessage.Type.IMAGE.ordinal()) {
            setContentView(getResources().getIdentifier("em_context_menu_for_image", "layout", getPackageName()));
        } else if (type == EMMessage.Type.VOICE.ordinal()) {
            setContentView(getResources().getIdentifier("em_context_menu_for_voice", "layout", getPackageName()));
        } else if (type == EMMessage.Type.VIDEO.ordinal()) {
            setContentView(getResources().getIdentifier("em_context_menu_for_video", "layout", getPackageName()));
        } else if (type == EMMessage.Type.FILE.ordinal()) {
            setContentView(getResources().getIdentifier("em_context_menu_for_location", "layout", getPackageName()));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    public void copy(View view) {
        setResult(RESULT_CODE_COPY);
        finish();
    }

    public void delete(View view) {
        setResult(RESULT_CODE_DELETE);
        finish();
    }

    public void forward(View view) {
        setResult(RESULT_CODE_FORWARD);
        finish();
    }

}
