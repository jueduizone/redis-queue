package com.jamie.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.jamie.model.Job;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;

/**
 * kryo 序列化工具类
 */
public class KryoUtil {
    private static KryoPool pool = null;

    static {
        KryoFactory factory = new KryoFactory() {
            public Kryo create() {
                Kryo kryo = new Kryo();
                kryo.setReferences(false);
                //把已知的结构注册到Kryo注册器里面，提高序列化/反序列化效率
                kryo.register(Job.class);
                kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                return kryo;
            }
        };
        pool = new KryoPool.Builder(factory).build();
    }

    /**
     * 序列化
     *
     * @param object
     * @return
     * @throws IOException
     */
    public static byte[] serialize(Object object) throws IOException {
        Kryo kryo = pool.borrow();
        byte[] buffer = new byte[2048];
        Output out = new Output(buffer);
        kryo.writeClassAndObject(out, object);
        out.close();
        pool.release(kryo);
        return out.toBytes();
    }


    /**
     * @param value
     * @return
     * @throws IOException
     */
    public static Object deserialize(byte[] value) throws IOException {
        Kryo kryo = pool.borrow();
        Input in = new Input(value);
        Object result = kryo.readClassAndObject(in);
        in.close();
        pool.release(kryo);
        return result;
    }
}
