package com.dev.controllers;

import com.dev.models.MyProductsModel;
import com.dev.objects.Auction;
import com.dev.objects.Offer;
import com.dev.objects.User;
import com.dev.responses.AuctionResponse;
import com.dev.responses.BasicResponse;
import com.dev.responses.MyProductsResponse;
import com.dev.utils.Persist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.dev.utils.Errors.ERROR_NO_SUCH_PRODUCT;
import static com.dev.utils.Errors.ERROR_NO_SUCH_TOKEN;

@RestController
public class MyProductsController {
    @Autowired
    private Persist persist;

    @RequestMapping(value = "get-my-products", method = RequestMethod.GET)
    public BasicResponse getMyProducts (String token) {
        BasicResponse response;
        User user = persist.getUserByToken(token);
        if (user != null){
            List<MyProductsModel> myProducts = persist.getMyProducts(token);
            response = new MyProductsResponse(true, null, myProducts);
        } else {
            response = new BasicResponse(false, ERROR_NO_SUCH_TOKEN);
        }
        return response;
    }
}