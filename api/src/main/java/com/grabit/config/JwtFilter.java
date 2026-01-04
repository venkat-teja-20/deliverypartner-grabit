package com.grabit.config;

import com.grabit.Utilities.JWTUtil;
import com.grabit.Utilities.Utility;
import com.grabit.auth.PermissionDTO;
import com.grabit.auth.RoleDTO;
import com.grabit.enums.CommonErrors;
import com.grabit.enums.PermissionsList;
import com.grabit.exception.APIError;
import com.grabit.exception.CustomException;
import com.grabit.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class JwtFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;

    public JwtFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String authToken= Utility.isNullOrEmpty(request.getHeader("Authorization"))?request.getHeader("authorization"):request.getHeader("Authorization");
            String token=null;
            String email=null;
            String role=null;
            if(authToken!=null && authToken.startsWith("JWT ")){
                token=authToken.substring(4);
                Claims claims=JWTUtil.getClaims(token);
                email=JWTUtil.getEmail(claims);
                role=JWTUtil.extractRole(claims);
                List<String> permissions=JWTUtil.getPermissions(claims);
                if (permissions == null || permissions.isEmpty()) {
                    throw new CustomException(Utility.buildErrorObject("PERMISSIONS_MISSING", "User do not have any valid permissions", 500, "jwtFilter"));
                }
            }

            if(token!=null && email!=null && role!=null && SecurityContextHolder.getContext().getAuthentication()==null){
                List<SimpleGrantedAuthority> authorities=new ArrayList<>();

                ResponseEntity<RoleDTO> roleAndPermissionsDetails = restTemplate.getForEntity(System.getenv("auth_url") + "/role/"+role+"/", RoleDTO.class);
                if (Utility.isNullOrEmpty(roleAndPermissionsDetails.getBody()) || Utility.isNullOrEmpty(roleAndPermissionsDetails.getBody().getRole()))
                    throw new CustomException(Utility.buildErrorObject("INVALID_RESPONSE", "response received from auth service while fetching role details is null or not valid", 500, "jwtFilter"));

                authorities.add(new SimpleGrantedAuthority(roleAndPermissionsDetails.getBody().getRole().name()));
                for(PermissionDTO permissionDTO:roleAndPermissionsDetails.getBody().getPermissions()){
                    authorities.add(new SimpleGrantedAuthority(permissionDTO.getPermission().name()));
                }
                UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(email,null,authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request,response);
        } catch (JwtAuthenticationException e){
            APIError apiError=e.getAuthenticationError();
            if(Utility.isNullOrEmpty(apiError))
                apiError=new APIError(CommonErrors.AUTHENTICATION_REQUIRED.toString(),CommonErrors.AUTHENTICATION_REQUIRED.getMessage());
            log.info("Authentication Unsuccessful : "+Utility.toJson(apiError));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            request.setAttribute("responseWriterFlag",true);
            response.getWriter().write(Utility.toJson(apiError));
        }
    }
}
