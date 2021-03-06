package com.bjzjns.hxplugin;

import android.content.Context;
import android.text.TextUtils;

import com.bjzjns.hxplugin.manager.HXManager;
import com.bjzjns.hxplugin.model.ConversationItemModel;
import com.bjzjns.hxplugin.model.ConversationListModel;
import com.bjzjns.hxplugin.model.HXUserModel;
import com.bjzjns.hxplugin.tools.GsonUtils;
import com.bjzjns.hxplugin.tools.LogUtils;
import com.bjzjns.hxplugin.tools.ToastUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.model.MessageExtModel;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class ZJNSHXPlugin extends CordovaPlugin {
    private static final String TAG = "ZJNSHXPlugin";
    // 初始化环信
    private static final String INIT_HX = "initEaseMobile";
    // 登录环信
    private static final String LOGIN = "login";
    // 退出环信
    private static final String LOGOUT = "logout";
    // 获取所有会话
    private static final String LOAD_ALL_CONVERSATION = "getAllConversations";
    // 删除会话
    private static final String DEL_CONVERSATION_ITEM = "delConversationItem";
    // 进入聊天
    private static final String GOTO_CHAT = "gotoChat";
    private static CordovaWebView mWebView;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mWebView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LogUtils.d(TAG, "action =" + action + ",args =" + args.toString());
        if (action.equals(INIT_HX)) {
            try {
                initHX();
                callbackContext.success("initEaseMobile success");
            } catch (Exception e) {
                callbackContext.error("initEaseMobile error:" + e.toString());
            }
            return true;
        } else if (action.equals(LOGIN)) {
            login(args.getString(0), args.getString(1), callbackContext);
            return true;
        } else if (action.equals(LOGOUT)) {
            logout(true, callbackContext);
            return true;
        } else if (action.equals(LOAD_ALL_CONVERSATION)) {
            loadAllConversation(callbackContext);
            return true;
        } else if (action.equals(DEL_CONVERSATION_ITEM)) {
            delConversationItem(args.getString(0), callbackContext);
            return true;
        } else if (action.equals(GOTO_CHAT)) {
            gotoChat(args.getString(0));
            return true;
        }
        return false;
    }

    private Context getContext() {
        return this.cordova.getActivity();
    }

    /**
     * 初始化环信
     */
    private void initHX() {
        LogUtils.d(TAG, "initHX");
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HXManager.getInstance().init(cordova.getActivity().getApplicationContext());
            }
        });
    }

    /**
     * 进入聊天
     *
     * @param sendVal
     */
    private void gotoChat(final String sendVal) {
        LogUtils.d(TAG, "gotoChat sendVal =" + sendVal);
        MessageExtModel extModel = GsonUtils.fromJson(sendVal, MessageExtModel.class);
        if (null == extModel || null == extModel.touser
                || TextUtils.isEmpty(extModel.touser.easemobile_id)) {
            ToastUtils.showShort(getContext(), getContext().getResources().getIdentifier("str_send_ext_error", "string", getContext().getPackageName()));
            return;
        }

        if (null == extModel.user || TextUtils.isEmpty(extModel.user.easemobile_id)) {
            ToastUtils.showShort(getContext(), getContext().getResources().getIdentifier("str_login_prompt", "string", getContext().getPackageName()));
            return;
        }
        this.cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                HXManager.getInstance().startChatActivity(getContext(), sendVal);
            }
        });
    }

    /**
     * 登录环信
     *
     * @param userName
     * @param password
     */
    private void login(String userName, String password, final CallbackContext callbackContext) {
        LogUtils.d(TAG, "login");
        HXUserModel userModel = new HXUserModel();
        userModel.userHXId = userName;
        userModel.password = password;
        HXManager.getInstance().loginHX(userModel, new EMCallBack() {
            @Override
            public void onSuccess() {
                LogUtils.d(TAG, "login success");
                callbackContext.success("login success");
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                LogUtils.d(TAG, "login error:" + i + ":" + s);
                callbackContext.error("login error:" + i + ":" + s);
            }

            @Override
            public void onProgress(int i, String s) {
            }
        });
    }

    /**
     * 退出环信
     *
     * @param unbindDeviceToken
     * @param callbackContext
     */
    private void logout(boolean unbindDeviceToken, final CallbackContext callbackContext) {
        LogUtils.d(TAG, "logout");
        HXManager.getInstance().logout(unbindDeviceToken, new EMCallBack() {

            @Override
            public void onSuccess() {
                LogUtils.d(TAG, "logout success");
                callbackContext.success("logout success");
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(int code, String error) {
                LogUtils.d(TAG, "logout error:" + code + ":" + error);
                callbackContext.error("logout error:" + code + ":" + error);
            }
        });
    }

    /**
     * 获取所有会话
     *
     * @param callbackContext
     */
    private void loadAllConversation(final CallbackContext callbackContext) {
        LogUtils.d(TAG, "loadAllConversation");
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConversationListModel conversationListModel = new ConversationListModel();
                    List<ConversationItemModel> conversationItemList = new ArrayList<ConversationItemModel>();
                    ConversationItemModel conversationItemModel;
                    EMMessage message;
                    for (EMConversation emConversation : HXManager.getInstance().loadConversationList()) {
                        conversationItemModel = new ConversationItemModel();
                        message = emConversation.getLastMessage();
                        conversationItemModel.conversationId = emConversation.conversationId();
                        conversationItemModel.unreadMessagesCount = emConversation.getUnreadMsgCount() + "";
                        conversationItemModel.timestamp = message.getMsgTime() + "";
                        String content = "";
                        if (EMMessage.Type.TXT == message.getType()) {
                            content = ((EMTextMessageBody) message.getBody()).getMessage();
                        }
                        conversationItemModel.messageBodyContent = content;
                        conversationItemModel.messageBodyType = message.getType().ordinal() + 1 + "";
                        String extContent = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXT, "");
                        MessageExtModel extModel = GsonUtils.fromJson(extContent, MessageExtModel.class);
                        conversationItemModel.ext = extModel;
                        conversationItemList.add(conversationItemModel);
                    }
                    conversationListModel.conversationList = conversationItemList;
                    LogUtils.d(TAG, "AllConversation gson data:" + GsonUtils.toJson(conversationListModel));
                    callbackContext.success(GsonUtils.toJson(conversationListModel));
                } catch (Exception e) {
                    LogUtils.d(TAG, "loadAllConversation exception:" + e.toString());
                    callbackContext.error("loadAllConversation exception:" + e.toString());
                }
            }
        });
    }

    /**
     * 删除会话
     *
     * @param sendVal
     * @param callbackContext
     */
    private void delConversationItem(final String sendVal, final CallbackContext callbackContext) {
        LogUtils.d(TAG, "delConversationItem");
        if (!TextUtils.isEmpty(sendVal)) {
            try {
                this.cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 删除此会话
                        HXManager.getInstance().delConversation(sendVal);
                        LogUtils.d(TAG, "delConversationItem success");
                        callbackContext.success("delConversationItem success");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.d(TAG, "delConversationItem exception:" + e.toString());
                callbackContext.error("delConversationItem exception:" + e.toString());
            }
        } else {
            LogUtils.d(TAG, "delConversationItem you not send data");
            callbackContext.error("you not send data");
        }
    }

    /**
     * 通知会话列表变化
     */
    public static void renewConversationList() {
        if (null != mWebView) {
            LogUtils.d(TAG, "renewConversationList");
            mWebView.loadUrl("javascript:renewConversationList()");
        }
    }

    /**
     * 进入设计师详情
     *
     * @param sendVal
     */
    public static void gotoDesignerDetail(String sendVal) {
        if (null != mWebView) {
            LogUtils.d(TAG, "gotoDesignerDetail sendVal=" + sendVal);
            mWebView.loadUrl("javascript:goToDesignerDetail(" + sendVal + ")");
        }
    }

    /**
     * 进入用户详情
     *
     * @param sendVal
     */
    public static void gotoUserDetail(String sendVal) {
        if (null != mWebView) {
            LogUtils.d(TAG, "gotoUserDetail sendVal=" + sendVal);
            mWebView.loadUrl("javascript:goToUserDetail(" + sendVal + ")");
        }
    }

    /**
     * 进入商品详情
     *
     * @param sendVal
     */
    public static void gotoProductDetail(String sendVal) {
        if (null != mWebView) {
            LogUtils.d(TAG, "gotoProductDetail sendVal=" + sendVal);
            mWebView.loadUrl("javascript:goToProductDetail(" + sendVal + ")");
        }
    }
}
