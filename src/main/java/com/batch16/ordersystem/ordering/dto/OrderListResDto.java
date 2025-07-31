package com.batch16.ordersystem.ordering.dto;

import com.batch16.ordersystem.common.constant.OrderStatus;
import com.batch16.ordersystem.ordering.domain.OrderDetail;
import com.batch16.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    List<OrderDetailResDto> orderDetails;

    public static OrderListResDto fromEntity(Ordering ordering){
        List<OrderDetailResDto> orderDetailResDtoList = new ArrayList<>();
        for (OrderDetail orderDetail : ordering.getOrderDetailList()){
            orderDetailResDtoList.add(OrderDetailResDto.fromEntity(orderDetail));
        }
        OrderListResDto dto = OrderListResDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(orderDetailResDtoList)
                .build();
        return dto;
    }
}
