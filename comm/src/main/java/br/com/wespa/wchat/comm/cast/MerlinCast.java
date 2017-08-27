package br.com.wespa.wchat.comm.cast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import br.com.wespa.wchat.comm.dto.MessageDTO;

/**
 * Created by dsouza on 26/08/17.
 */

public class MerlinCast {

    private final Gson gson;

    public MerlinCast() {
        gson = new Gson();
    }

    public <A, B> List<B> toJson(List<A> src, Class<B> dstClass) {
        ArrayList<B> result = new ArrayList<>();

        if (src.isEmpty())
            return result;

        try {
            Method castMethod = getClass().getMethod("Cast", src.get(0).getClass(), dstClass);
            B dstObj = dstClass.newInstance();

            for (A srcObj : src)
                result.add((B)castMethod.invoke(this, srcObj, dstObj));

            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public <B> B fromJson(String src, Class<B> dstClass) {
        if (src == null)
            return null;

        return gson.fromJson(src, dstClass);
    }

    public <A> String toJson(A src) {
        if (src == null)
            return null;

        return gson.toJson(src);
    }

}
