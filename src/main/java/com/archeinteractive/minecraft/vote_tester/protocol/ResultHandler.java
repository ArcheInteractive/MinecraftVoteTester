package com.archeinteractive.minecraft.vote_tester.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.json.JSONObject;

public class ResultHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object data) throws Exception {
        System.out.println("Parsing result.");
        String json = data.toString();
        JSONObject result = new JSONObject(json);
        System.out.println(result.toString());
    }

}
