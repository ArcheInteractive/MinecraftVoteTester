package com.archeinteractive.minecraft.vote_tester.protocol;

import com.archeinteractive.minecraft.vote_tester.Main;
import com.archeinteractive.minecraft.vote_tester.model.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class HandshakeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf data) throws Exception {
        String handshake = data.toString(CharsetUtil.UTF_8);
        Main.Session session = ctx.channel().attr(Main.SESSION_KEY).get();
        if (!(handshake == null || handshake.isEmpty())) {
            handshake = handshake.replace("\n", "");
            String[] args = handshake.split(" ");
            System.out.println(String.format("Args: %s", Arrays.toString(args)));
            if (args.length >= 2 && session.validate()) {
                System.out.println(handshake);

                long time = System.currentTimeMillis();
                Payload payload = new Payload(session.getServiceName(), session.getUserName(), "127.0.0.1", time, args.length > 2 ? args[2] : null);
                JSONObject jsonPayload = payload.serialize();

                System.out.println("Generating key spec...");
                Charset charset = StandardCharsets.UTF_8;
                Mac hmac = Mac.getInstance("HmacSHA256");
                SecretKeySpec spec = new SecretKeySpec(session.getPublicKey().getBytes(charset), "HmacSHA256");

                System.out.println("Initializing HMac...");
                hmac.init(spec);

                System.out.println("Encoding signature...");
                String signature = Base64.getEncoder().encodeToString(hmac.doFinal(jsonPayload.toString().getBytes(charset)));
                System.out.println(String.format("Encoded signature: %s", signature));

                System.out.println("Constructing message...");
                JSONObject message = new JSONObject();
                message.put("payload", jsonPayload.toString());
                message.put("signature", signature);

                short op = 0x733A;
                short length = (short) message.toString().length();

                System.out.println("Sending payload: " + message.toString());
                ByteBuf buffer = Unpooled.copiedBuffer(
                        new byte[]{(byte)(op >> 8), (byte)op},
                        new byte[]{(byte)(length >> 8), (byte)length},
                        message.toString().getBytes(charset)
                );
                ctx.writeAndFlush(buffer);
                ctx.pipeline().remove(this);
                System.out.println("Sent payload!");
            }
        }
    }

}
