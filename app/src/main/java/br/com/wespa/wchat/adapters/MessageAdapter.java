package br.com.wespa.wchat.adapters;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.wespa.wchat.R;
import br.com.wespa.wchat.persistency.models.Message;

/**
 * Created by dsouza on 26/08/17.
 */

public class MessageAdapter extends BaseAdapter {

    private final ArrayList<Bubble> mMsgs;
    private final LayoutInflater mInflater;
    private final String mCurrentUser;
    private String mLastSender;


    public MessageAdapter(LayoutInflater inflater, String currentUser) {
        mMsgs = new ArrayList<>();
        mInflater = inflater;
        mCurrentUser = currentUser;
    }

    @Override
    public int getCount() {
        return mMsgs.size();
    }

    @Override
    public Object getItem(int position) {
        return mMsgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Bubble b = mMsgs.get(position);

        if (view == null)
            view = mInflater.inflate(R.layout.message_bubble, null);

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView body = (TextView) view.findViewById(R.id.body);

        int gravity = b.isSelf ? Gravity.RIGHT : Gravity.LEFT;

        if (b.isHeader) {
            body.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            title.setGravity(gravity);
            title.setText(b.text);
        } else {
            body.setVisibility(View.VISIBLE);
            title.setVisibility(View.GONE);
            body.setGravity(gravity);
            body.setText(b.text);
        }

        return view;
    }

    public void add(Message msg) {
        if (!msg.getSender().equals(mLastSender)) {
            mLastSender = msg.getSender();

            Bubble b = new Bubble();
            b.isSelf = mLastSender.equals(mCurrentUser);
            b.text = b.isSelf ? "YOU" : mLastSender.toUpperCase();
            b.isHeader = true;
            mMsgs.add(b);
        }

        Bubble b = new Bubble();
        b.text = msg.getContent();
        b.isSelf = msg.getSender().equals(mCurrentUser);
        b.isHeader = false;
        mMsgs.add(b);
    }

    public void clear() {
        mLastSender = null;
        mMsgs.clear();
    }

    public class Bubble {

        public String text;

        public boolean isHeader;

        public boolean isSelf;

    }

}
