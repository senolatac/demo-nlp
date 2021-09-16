package com.example.demonlp.service.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

/**
 * @author sa
 * @date 14.09.2021
 * @time 14:31
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HttpRequestExecutor
{

    private final RestOperations restOperations;

    public <T> T executeRequest(RequestEntity requestEntity, Class<T> resultClass)
    {
        try
        {
            RestOperations restTemplate = restOperations;

            ResponseEntity<T> result = restTemplate.exchange(requestEntity, resultClass);

            return result.getBody();
        }
        catch (HttpClientErrorException | HttpServerErrorException ex)
        {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND)
            {
                log.info("Requested resource was not found url: {}", requestEntity.getUrl());
                throw new RestClientException("Requested resource was not found", ex);
            }

            log.warn("Couldn't get successful result from http request status:{} url: {}", ex.getStatusCode(), requestEntity.getUrl());

            throw new RestClientException("Couldn't get successful result from http request", ex);
        }
        catch (ResourceAccessException ex)
        {
            log.warn("Couldn't execute http request with SSL error for url: {}", requestEntity.getUrl());

            throw new RestClientException("Couldn't execute http request with SSL error", ex);
        }
        catch (Exception ex)
        {
            log.error("Unknown error occurred while executing http request for url: {}", requestEntity.getUrl(), ex);

            throw new IllegalStateException("Unknown error occurred while executing http request", ex);
        }
    }
}
