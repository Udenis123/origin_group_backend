package com.org.group.services.emailAndJwt;

import com.org.group.model.Users;
import com.org.group.subscription.SubscriptionStatus;
import org.springframework.stereotype.Service;

@Service
public class PlanFilterServices {
    public String getPlanFiltered(Users user){
        return user.getSubscriptions().stream()
                .filter(sub -> !sub.getStatus().equals(SubscriptionStatus.EXPIRED)) // Filter out expired subscriptions
                .map(sub -> sub.getPlan().toString())
                .max((plan1, plan2) -> {
                    // Define the priority order
                    int priority1 = getPlanPriority(plan1);
                    int priority2 = getPlanPriority(plan2);
                    return Integer.compare(priority1, priority2);
                })
                .orElse("FREE"); // Default to FREE if no active subscriptions

    }



    private int getPlanPriority(String plan) {
        switch (plan) {
            case "IMENA": return 4;
            case "PREMIUM": return 3;
            case "BASIC": return 2;
            case "FREE": return 1;
            default: return 0;
        }
    }


}
