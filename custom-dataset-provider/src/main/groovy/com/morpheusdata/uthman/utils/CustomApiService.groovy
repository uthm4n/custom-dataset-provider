package com.morpheusdata.uthman.utils

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.library.MorpheusOptionTypeService
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.core.util.HttpApiClient.RequestOptions
import com.morpheusdata.core.util.MorpheusUtils
import com.morpheusdata.response.ServiceResponse
import groovy.util.logging.Slf4j

@Slf4j
class CustomApiService {

    Plugin plugin
    MorpheusContext morpheus

    CustomApiService(Plugin plugin, MorpheusContext morpheus) {
        this.plugin = plugin
        this.morpheus = morpheus
    }

    // params = [url: "", endpoint: "", bearer_token: "", basic: "", method: ""]
    ServiceResponse getData(Map params) {
        HttpApiClient httpClient = new HttpApiClient()
        RequestOptions opts = new RequestOptions()

        def customApiResponse = new ServiceResponse()
        try {
            opts.headers = params?.basic ? ["Authorization": "Basic ${params.basic}"] :
                    params?.bearer_token ? ["Authorization": "Bearer ${params.bearer_token}"] : [:]

            opts.ignoreSSL = false

            def results = httpClient.callJsonApi("${params?.url}", "${params?.endpoint}", null, null, opts, "${params.method}")
            println "RESULTS: $results"

            customApiResponse.success = results.success
            println "customApiResponse.success: ${customApiResponse.success}, results.success: ${results.success}"

            customApiResponse.data = results.data
            customApiResponse.msg = results.msg
            customApiResponse.errors = results.errors

            log.info("Full API Response: ${customApiResponse}")

            if (customApiResponse.data) {
                log.info("customApiResponse Data: ${MorpheusUtils.getJson(customApiResponse.data)}")
            } else {
                log.warn("API response data is null or empty.")
            }

        } catch (Exception ex) {
            log.error("Exception while fetching data from API: ${ex.message}", ex)
        } finally {
            httpClient.shutdownClient()
        }
        return customApiResponse
    }

    ServiceResponse test(Map params) {
        params = [url: 'https://cat-fact.herokuapp.com', endpoint: '/facts', method: 'GET']
        def results = getData(params)
        log.info("CustomApiService.test() --> results: ${results}")
        return results
    }
}