
package com.dev.utils;


import com.dev.objects.Message;
import com.dev.objects.Product;
import com.dev.objects.User;

import com.dev.models.MyOfferModel;
import com.dev.objects.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Persist {

    private final SessionFactory sessionFactory;
    @Autowired
    private Utils utils;
    @Autowired
    public Persist (SessionFactory sf) {
        this.sessionFactory = sf;
    }
    public void initBasicDetails() {
        //setAdmin();
        //setAuctions();
    }
    public void setAdmin() {
        addUser("admin", "123456");
        addUser("a", "123456");
        addUser("b", "123456");
        addUser("c", "123456");
    }
    public void setAuctions() {
        Product p1 = new Product(
                "table",
                "work table",
                "https://res.cloudinary.com/shufersal/image/upload/f_auto,q_auto/v1551800922/prod/product_images/products_zoom/XOZ56_Z_P_7290015745376_1.png",
                100,
                getUserByID(2)
        );
        Product p2 = new Product(
                "table",
                "work table",
                "https://res.cloudinary.com/shufersal/image/upload/f_auto,q_auto/v1551800922/prod/product_images/products_zoom/XOZ56_Z_P_7290015745376_1.png",
                100,
                getUserByID(2)
        );

        Auction a1 = new Auction(p1);
        Auction a2 = new Auction(p2);
        saveAuction(a1);
        saveAuction(a2);
        Offer o1 = new Offer(100, getUserByID(4), a1);
        Offer o2 = new Offer(120, getUserByID(3), a1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Offer o3 = new Offer(120, getUserByID(4), a1);
        Offer o4 = new Offer(10, getUserByID(4), a2);
        Offer o5 = new Offer(12, getUserByID(3), a2);
        saveOffer(o1);saveOffer(o2);saveOffer(o3);saveOffer(o4);saveOffer(o5);

        System.out.println(getOffersByAuctionID(1));
        System.out.println("-----");
        Auction a = getAuctionByProductID(1);
        a.setIsOpen(false);
        updateAuction(a);
        System.out.println(getMyOffers("5D96A0ED166A05B49DA80A031752FE80"));
        System.out.println(getAuctionByProductID(1).getWinnerOffer(this));

    }

    public User getUserByUsername (String username) {
        User found;
        Session session = sessionFactory.openSession();
        found = (User) session.createQuery("FROM User WHERE username = :username")
                .setParameter("username", username)
                .uniqueResult();
        session.close();
        return found;
    }

    public void saveUser (User user) {
        Session session = sessionFactory.openSession();
        session.save(user);
        session.close();
    }
    public void saveProduct (Product product) {
        Session session = sessionFactory.openSession();
        session.save(product);
        session.close();
    }
    public void saveAuction (Auction auction) {
        Session session = sessionFactory.openSession();
        saveProduct(auction.getProductObj());
        session.save(auction);
        session.close();
    }
    public void updateAuction (Auction auction) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(auction);
        transaction.commit();
        session.close();
    }
    public void saveOffer (Offer offer) {
        Session session = sessionFactory.openSession();
        session.save(offer);
        session.close();
    }

    public User getUserByUsernameAndToken (String username, String token) {
        User found;
        Session session = sessionFactory.openSession();
        found = (User) session.createQuery("FROM User WHERE username = :username " +
                        "AND token = :token")
                .setParameter("username", username)
                .setParameter("token", token)
                .uniqueResult();
        session.close();
        return found;
    }

    public List<User> getAllUsers () {
        Session session = sessionFactory.openSession();
        List<User> allUsers = session.createQuery("FROM User ").list();
        session.close();
        return allUsers;
    }

    public User getUserByToken (String token) {
        Session session = sessionFactory.openSession();
        User user = (User) session.createQuery("From User WHERE token = :token")
                .setParameter("token", token)
                .uniqueResult();
        session.close();
        return user;
    }

    public User getUserByID (int id) {
        Session session = sessionFactory.openSession();
        User user = (User) session.createQuery("From User WHERE id = :id")
                .setParameter("id", id)
                .uniqueResult();
        session.close();
        return user;
    }


    public void addUser(String userName, String password) {
        if (!userNameExist(userName)) {
            User userObject = new User();
            userObject.setUsername(userName);
            String token = utils.createHash(userName, password);
            userObject.setToken(token);
            saveUser(userObject);
        }
    }
    public boolean userNameExist(String userName) {
        boolean exist = false;
        Session session = sessionFactory.openSession();
        List<User> users = session.createQuery("FROM User where username= :userName")
                .setParameter("userName", userName).list();
        session.close();
        if (users.size() == 1) {
            exist = true;
        }
        return exist;
    }


    public List<Product> getMyProductsFromTable (String token) {
        Session session = sessionFactory.openSession();
        List<Product> myProducts = session.createQuery("FROM Auction WHERE User.token = :token").list();
        session.close();
        return myProducts;
    }

    public List<Offer> getOffersByAuctionID(int auctionID){
        Session session = sessionFactory.openSession();
        List<Offer> offers = session.createQuery("FROM Offer where auction.id= :auctionID")
                .setParameter("auctionID", auctionID).list();
        session.close();
        return offers;
    }

    public Auction getAuctionByProductID(int productID) {
        Session session = sessionFactory.openSession();
        Auction auction = (Auction) session.createQuery("FROM Auction where productObj.id= :productID")
                .setParameter("productID", productID).uniqueResult();
        session.close();
        return auction;
    }

    public List<MyOfferModel> getMyOffers(String token) {
        Session session = sessionFactory.openSession();
        List<Auction> auctions = session.createQuery("FROM Auction").list();
        session.close();
        List<MyOfferModel> myOffers = new ArrayList<>();
        auctions.stream()
                .flatMap(auction -> getOffersByAuctionID(auction.getId()).stream().filter(offer -> offer.getOffers().getToken().equals(token)))
                .forEach(userOffer -> myOffers.add(new MyOfferModel(userOffer, this)));
        return myOffers;
    }
}
