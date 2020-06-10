package android.demo.network;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import chat21.android.demo.R;


public class ProgressDialogCustom extends Dialog {

    static ProgressDialogCustom show(Context context,
                                     CharSequence title, CharSequence message) {
        return show(context, title, message, true);
    }


    public static ProgressDialogCustom show(Context context,
                                            CharSequence title, CharSequence message, boolean indeterminate,
                                            boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }


    private static ProgressDialogCustom show(Context context,
                                             CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    private static ProgressDialogCustom show(Context context,
                                             CharSequence title,
                                             CharSequence message,
                                             boolean indeterminate,
                                             boolean cancelable,
                                             OnCancelListener cancelListener) {
        ProgressDialogCustom dialog = new ProgressDialogCustom(context);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        /* The next line will add the ProgressBar to the dialog. */

        ProgressBar v = new ProgressBar(context);
        v.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimary)
                , android.graphics.PorterDuff.Mode.MULTIPLY);

        FrameLayout ll = new FrameLayout(context);

        ll.setBackgroundResource(R.drawable.progress_bg);

        FrameLayout.LayoutParams dsdsds = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        dsdsds.gravity = Gravity.CENTER;

        v.setLayoutParams(dsdsds);

        ll.addView(v);

        FrameLayout.LayoutParams qewwew = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        qewwew.gravity = Gravity.CENTER;

        dialog.addContentView(ll, qewwew);

        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dialog;
    }

    private ProgressDialogCustom(Context context) {
        super(context, R.style.Theme_Transparent4);
        ProgressBar v = new ProgressBar(context);
        v.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);

        FrameLayout ll = new FrameLayout(context);

        ll.setBackgroundResource(R.drawable.progress_bg);

        FrameLayout.LayoutParams dsdsds = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        dsdsds.gravity = Gravity.CENTER;

        v.setLayoutParams(dsdsds);

        ll.addView(v);

        FrameLayout.LayoutParams qewwew = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        qewwew.gravity = Gravity.CENTER;

        addContentView(ll, qewwew);
    }

    private static ProgressDialogCustom loadingDialog;

    public static void startLoadingDialog(Context pContext) {
        try {
            loadingDialog = ProgressDialogCustom.show(pContext, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void endLoadingDialog() {
        try {
            if (null != loadingDialog)
                loadingDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}