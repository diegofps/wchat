package br.com.wespa.wchat.persistency.managers;

import br.com.wespa.wchat.persistency.models.Message;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by dsouza on 26/08/17.
 */

public class Messages {

    public static void allAsync(RealmChangeListener<RealmResults<Message>> listener) {

    }

}
