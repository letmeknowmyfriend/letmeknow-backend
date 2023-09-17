//package com.letmeknow.interceptor;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.HandlerMapping;
//import com.letmeknow.dto.order.OrderDto;
//import com.letmeknow.enumstorage.errormessage.PaymentErrorMessage;
//import com.letmeknow.enumstorage.errormessage.StoreErrorMessage;
//import com.letmeknow.enumstorage.status.StoreStatus;
//import com.letmeknow.message.Message;
//import com.letmeknow.service.OrderService;
//import com.letmeknow.service.store.StoreService;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class OrderInterceptor implements HandlerInterceptor {
//    private final OrderService orderService;
//    private final StoreService storeService;
//
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
//        Long memberId = (Long) pathVariables.get("memberId");
//        Long storeId = (Long) pathVariables.get("storeId");
//        Long storeTableId = (Long) pathVariables.get("storeTableId");
//        Long orderId = (Long) pathVariables.get("orderId");
//
//        //이미 결제중이면 결제 페이지로 redirect
//        boolean paymentStarted = isPaymentStarted(memberId, storeId, storeTableId, orderId, response);
//        if (!paymentStarted) {
//            return false;
//        }
//
//        //가게가 닫혀있으면, 가게 정보 페이지로 redirect
//        boolean storeOpen = isStoreOpen(memberId, storeId, response);
//        if (!storeOpen) {
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean isPaymentStarted(Long memberId, Long storeId, Long storeTableId, Long orderId, HttpServletResponse response) throws IOException {
//        //이미 결제중이면 결제 페이지로 redirect
//        //진행중인 결제가 존재하면
//        OrderDto orderDtoById = orderService.findOrderDtoByIdAndStoreId(orderId, storeId);
//        if (orderDtoById.getPaymentId() != null) {
//            Message message = Message.builder()
//                    .message(PaymentErrorMessage.PAYMENT_ALREADY_EXIST.getMessage())
//                    .href("/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId + "/payments/" + orderDtoById.getPaymentId())
//                    .build();
//
//            //json 형식으로 메시지 출력
//            returnJson(response, message);
//
//            //redirect
//            response.sendRedirect(message.getHref());
//
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean isStoreOpen(Long memberId, Long storeId, HttpServletResponse response) throws IOException {
//        //가게가 닫혀있으면, 가게 정보 페이지로 redirect
//        if (storeService.findStoreDtoById(storeId).getStoreStatus().equals(StoreStatus.CLOSE.toString())) {
//            Message message = Message.builder()
//                    .message(StoreErrorMessage.STORE_CLOSED.getMessage())
//                    .href("/members/" + memberId + "/stores/" + storeId)
//                    .build();
//
//            //json 형식으로 메시지 출력
//            returnJson(response, message);
//
//            //redirect
//            response.sendRedirect(message.getHref());
//
//            return false;
//        }
//
//        return true;
//    }
//
//    private void returnJson(HttpServletResponse response, Message message) throws IOException {
//        //json 형식으로 메시지 출력
//        //content-type
//        response.setContentType("application/json");
//        response.setCharacterEncoding("utf-8");
//        String json = objectMapper.writeValueAsString(message);
//        response.getWriter().write(json);
//    }
//}
