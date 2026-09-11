package com;


import com.agents.ControllerAgent;
import com.agents.QueryAgent;
import com.agents.RouterAgent;
import com.dto.ChatRequest;
import com.dto.ChatResponse;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;


@Path("/api/")  
public class RestController {

    @Inject RouterAgent routerAgent;
    @Inject QueryAgent queryAgent;
    @Inject ControllerAgent controllerAgent;

    @POST
    @Path("chat")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response chat(ChatRequest request){

        String userId = request.sessionId() != null? request.sessionId():UUID.randomUUID().toString();

        RouterAgent.CategoryCustom category = routerAgent.findeCategory(request.message());

        String response = switch(category){
            case GETVALU ->  queryAgent.chat(userId,request.message());
            case SETVALU ->  controllerAgent.chat(userId,request.message()); 
            case NAME -> routerAgent.sendYourName(request.message());
            case GENERAL -> routerAgent.resolvRequest(userId,request.message());
            default -> throw new IllegalArgumentException("Unexpected value: " + category);
        };

        return Response.ok(new ChatResponse(userId, response, category.name())).build();
    }


}
