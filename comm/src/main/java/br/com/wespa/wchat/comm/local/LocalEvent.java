package br.com.wespa.wchat.comm.local;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by dsouza on 26/08/17.
 */

public class LocalEvent {

    private final Context mContext;
    private final Intent mIntent;

    public LocalEvent(Context context, String action) {
        mContext = context;
        mIntent = new Intent(action);
    }

    public LocalEvent add(String name, Parcelable value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, long[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, byte value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, double[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, CharSequence value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, boolean[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, int value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, char[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, byte[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, Parcelable[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, Bundle value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, CharSequence[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, float[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, double value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, int[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, String[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, short[] value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, boolean value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, String value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, long value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, char value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, Serializable value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, float value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(String name, short value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent add(Bundle extras) {
        mIntent.putExtras(extras);
        return this;
    }

    public LocalEvent addCharSequenceArrayList(String name, ArrayList<CharSequence> value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent addIntegerArrayList(String name, ArrayList<Integer> value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent addParcelableArrayList(String name, ArrayList<? extends Parcelable> value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public LocalEvent addStringArrayList(String name, ArrayList<String> value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public void send() {
        mContext.sendBroadcast(mIntent);
    }

}
