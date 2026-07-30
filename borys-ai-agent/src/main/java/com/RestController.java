package com;

import com.agents.ControllerAgent;
import com.agents.QueryAgent;
import com.agents.RouterAgent;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@Path("/api/")  
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class RestController {

    @Inject RouterAgent routerAgent;
    @Inject QueryAgent queryAgent;
    @Inject ControllerAgent controllerAgent;

    @POST
    @Path("chat")
    public String chat(String messasge){

        RouterAgent.CategoryCustom category = routerAgent.findeCategory(messasge);

        return switch(category){
            case GETVALU ->  queryAgent.chat(messasge);
            case SETVALU ->  controllerAgent.chat(messasge); 
            case NAME -> routerAgent.sendYourName(messasge);
            case GENERAL -> routerAgent.resolvRequest(messasge);
            default -> throw new IllegalArgumentException("Unexpected value: " + category);
        };

    }


}
