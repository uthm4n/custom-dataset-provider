package com.morpheusdata.uthman.utils

import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.core.util.HttpApiClient.RequestOptions
import com.morpheusdata.response.ServiceResponse
import groovy.util.logging.Slf4j

@Slf4j
class CustomApiServiceOriginal {

    ServiceResponse getDataFrom(String url, String endpoint) {
        def client = new HttpApiClient()
        def requestOpts = new RequestOptions()

        def response = new ServiceResponse()
        try {
            def customApiResponse = client.callJsonApi(url, endpoint, requestOpts, 'GET')
            log.info("customApiResponse: ${customApiResponse}")
            log.info("customApiResponse.data: ${customApiResponse.data}")
            requestOpts.with {
                headers = [:]
                body = [:]
                queryParams = [:]
            }
            response.success = customApiResponse.success
        //    response.data = customApiResponse.data
            log.info("customApiService.getData: ${response.data}")
        } catch (Exception ex) {
            log.debug("customApiService.getData: ${ex}\r\n${ex.printStackTrace()}")
        } finally {
            client.shutdownClient()
        }
        return response
    }


}
