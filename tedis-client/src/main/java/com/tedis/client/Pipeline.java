package com.tedis.client;

import com.tedis.client.common.Cmd;
import com.tedis.client.common.TedisFuture;
import com.tedis.client.pool.TedisPool;
import com.tedis.protocol.Command;
import com.tedis.protocol.Commands;
import com.tedis.protocol.Request;
import com.tedis.protocol.Results;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class Pipeline extends AbstractConnection<Results> {

    public static final AttributeKey<TedisFuture<Results>> FUTURE_KEY = AttributeKey.valueOf("future");
    private Commands cmds;


    public Pipeline(Channel channel, TedisPool pool) {
        super(channel, pool);
        cmds = new Commands();
    }

    @Override
    TedisFuture<Results> execute(Cmd cmd, String... params) {
        Command c = generateCmd(cmd, params);
        cmds.add(c);
        return null;
    }

    public TedisFuture<Results> submit() {
        Request<Commands> req = new Request<>(cmds);
        TedisFuture<Results> future = new TedisFuture<>();
        channel.attr(FUTURE_KEY).set(future);
        channel.attr(RESULT_NUM_KEY).set(cmds.getCmds().size());
        try {
            channel.writeAndFlush(req).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cmds.clear();
        return future;
    }

}
