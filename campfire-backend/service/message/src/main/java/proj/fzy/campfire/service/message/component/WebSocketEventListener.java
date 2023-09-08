package proj.fzy.campfire.service.message.component;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import proj.fzy.campfire.model.dto.AuthenticationInfo;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.RedisUtils;

@Component
public class WebSocketEventListener {

    @EventListener
    public void sessionConnectedEventListener(SessionConnectedEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        System.out.println(headerAccessor.getUser().getName() + ":" + headerAccessor.getCommand());
    }

    @EventListener
    public void sessionSubscribeEventListener(SessionSubscribeEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        System.out.println(headerAccessor.getUser().getName() + ":" + headerAccessor.getCommand());
    }

}
