package org.jboss.narayana.examples.basic;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;

public class HornetqStoreExample extends TransactionExample {
    private static final String storeClassName =  com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor.class.getName();
    private static final String storeDir = "target/HornetqStore";

    public static void main(String[] args) throws Exception {
        setupStore();
        TransactionExample.main(new String[0]);
        checkSuccess();
    }

    @BeforeClass
    public static void setupStore() throws Exception {
        File hornetqStoreDir = new File(storeDir);

        BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class)
                    .setStoreDir(hornetqStoreDir.getCanonicalPath());

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeClassName);

        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "default").setObjectStoreType(storeClassName);
        // TODO figure out why we can't use the hornetqStore as the communications store
        //BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeClassName);

    }

    @AfterClass
    public static void checkSuccess() throws Exception {
        File f = new File(storeDir);
        Assert.assertTrue(f.exists());
    }
}
