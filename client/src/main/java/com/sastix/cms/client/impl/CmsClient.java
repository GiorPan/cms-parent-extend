/*
 * Copyright(c) 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sastix.cms.client.impl;

import com.hazelcast.core.HazelcastInstance;
import com.sastix.cms.client.CacheClient;
import com.sastix.cms.client.ContentClient;
import com.sastix.cms.client.LockClient;
import com.sastix.cms.common.Constants;
import com.sastix.cms.common.api.CacheApi;
import com.sastix.cms.common.api.ContentApi;
import com.sastix.cms.common.api.LockApi;
import com.sastix.cms.common.cache.CacheDTO;
import com.sastix.cms.common.cache.QueryCacheDTO;
import com.sastix.cms.common.cache.RemoveCacheDTO;
import com.sastix.cms.common.cache.exceptions.CacheValidationException;
import com.sastix.cms.common.cache.exceptions.DataNotFound;
import com.sastix.cms.common.client.ApiVersionClient;
import com.sastix.cms.common.client.RetryRestTemplate;
import com.sastix.cms.common.content.*;
import com.sastix.cms.common.content.exceptions.ContentValidationException;
import com.sastix.cms.common.content.exceptions.ResourceAccessError;
import com.sastix.cms.common.content.exceptions.ResourceNotFound;
import com.sastix.cms.common.content.exceptions.ResourceNotOwned;
import com.sastix.cms.common.dataobjects.ResponseDTO;
import com.sastix.cms.common.dataobjects.VersionDTO;
import com.sastix.cms.common.lock.LockDTO;
import com.sastix.cms.common.lock.NewLockDTO;
import com.sastix.cms.common.lock.QueryLockDTO;
import com.sastix.cms.common.lock.exceptions.LockNotAllowed;
import com.sastix.cms.common.lock.exceptions.LockNotFound;
import com.sastix.cms.common.lock.exceptions.LockNotHeld;
import com.sastix.cms.common.lock.exceptions.LockValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@ComponentScan("com.sastix.cms.client.config")
public class CmsClient implements ContentClient, LockClient, CacheClient, BeanFactoryAware {
    private Logger LOG = (Logger) LoggerFactory.getLogger(CmsClient.class);

    @Autowired
    @Qualifier("CmsApiVersionClient")
    ApiVersionClient apiVersionClient;

    @Autowired
    @Qualifier("CmsRestTemplate")
    RetryRestTemplate retryRestTemplate;

    Optional<HazelcastInstance> hazelcastInstance;

    private String getUrlRoot() {
        StringBuffer url = new StringBuffer(apiVersionClient.getApiUrl()).append("/");
        return url.toString();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        try {
            HazelcastInstance hz = beanFactory.getBean(HazelcastInstance.class);
            hazelcastInstance = Optional.of(hz);
            LOG.info("A local HZ instance (uid={}) can be found/used on this service.", hazelcastInstance.get().getLocalEndpoint().getUuid());
        } catch (final Exception e) {
            LOG.warn("Hazelcast Instance not found on this Server");
            hazelcastInstance = Optional.empty();
        }
    }

    @Override
    public VersionDTO getApiVersion() {
        return apiVersionClient.getApiVersion();
    }

    @Override
    public LockedResourceDTO lockResource(ResourceDTO resourceDTO) throws ResourceNotFound, ResourceNotOwned, ResourceAccessError, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.LOCK_RESOURCE_DTO;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + resourceDTO.toString());
        LockedResourceDTO lockedResourceDTO = retryRestTemplate.postForObject(url, resourceDTO, LockedResourceDTO.class);
        LOG.trace("Response: " + lockedResourceDTO.toString());
        return lockedResourceDTO;
    }

    @Override
    public void unlockResource(LockedResourceDTO lockedResourceDTO) throws ResourceNotFound, ResourceNotOwned, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.UNLOCK_RESOURCE_DTO;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + lockedResourceDTO.toString());
        retryRestTemplate.postForObject(url, lockedResourceDTO, LockedResourceDTO.class);
    }

    @Override
    public LockedResourceDTO renewResourceLock(LockedResourceDTO lockedResourceDTO) throws ResourceNotFound, ResourceNotOwned, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.RENEW_RESOURCE_DTO_LOCK;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + lockedResourceDTO.toString());
        LockedResourceDTO newLockedResourceDTO = retryRestTemplate.postForObject(url, lockedResourceDTO, LockedResourceDTO.class);
        LOG.trace("Response: " + newLockedResourceDTO.toString());
        return newLockedResourceDTO;
    }

    @Override
    public ResourceDTO createResource(CreateResourceDTO createResourceDTO) throws ResourceAccessError, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.CREATE_RESOURCE;
        LOG.trace("API call: " + url);
        //LOG.trace("Request: " + createResourceDTO.toString());
        ResourceDTO resourceDTO = retryRestTemplate.postForObject(url, createResourceDTO, ResourceDTO.class);
        //LOG.trace("Response: " + resourceDTO.toString());
        return resourceDTO;
    }

    @Override
    public LockedResourceDTO updateResource(UpdateResourceDTO updateResourceDTO) throws ResourceNotOwned, ResourceAccessError, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.UPDATE_RESOURCE;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + updateResourceDTO.toString());
        LockedResourceDTO lockedResourceDTO = retryRestTemplate.postForObject(url, updateResourceDTO, LockedResourceDTO.class);
        LOG.trace("Response: " + lockedResourceDTO.toString());
        return lockedResourceDTO;
    }

    @Override
    public ResourceDTO queryResource(ResourceQueryDTO resourceQueryDTO) throws ResourceAccessError, ResourceNotFound, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.QUERY_RESOURCE;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + resourceQueryDTO.toString());
        ResourceDTO resourceDTO = retryRestTemplate.postForObject(url, resourceQueryDTO, ResourceDTO.class);
        LOG.trace("Response: {}", resourceDTO);
        return resourceDTO;
    }

    @Override
    public ResourceDTO deleteResource(LockedResourceDTO lockedResourceDTO) throws ResourceNotOwned, ResourceAccessError, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.DELETE_RESOURCE;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + lockedResourceDTO.toString());
        ResourceDTO resourceDTO = retryRestTemplate.postForObject(url, lockedResourceDTO, ResourceDTO.class);
        LOG.trace("Response: " + resourceDTO.toString());
        return resourceDTO;
    }

    @Override
    public byte[] getData(DataDTO dataDTO) throws ResourceAccessError, ContentValidationException {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_DATA;
        LOG.trace("API call: " + url);
        LOG.trace("Request: " + dataDTO.toString());
        return retryRestTemplate.postForObject(url, dataDTO, byte[].class);

    }

    @Override
    public ResponseDTO getDataStream(DataDTO dataDTO) throws ResourceAccessError, ContentValidationException, IOException {
        return getData(dataDTO.getResourceURI());
    }


    @Override
    public ResponseDTO getData(final String uri) throws IOException {
        final String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_DATA + "/" + uri;
        URLConnection urlConnection = new URL(url).openConnection();
        final Map<String, List<String>> headers = urlConnection.getHeaderFields();
        final InputStream inputStream = urlConnection.getInputStream();
        final Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(ResponseDTO.CONTENT_TYPE, headers.get(ResponseDTO.CONTENT_TYPE).get(0));
        return new ResponseDTO(inputStream, responseHeaders);
    }

    @Override
    public ResponseDTO getMultiPartData(final String uri, final Map<String, List<String>> reqHeaders) throws IOException {
        final String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_MULTIPART_DATA + "/" + uri;

        final HttpHeaders headers = new HttpHeaders();
        headers.putAll(reqHeaders);

        final HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        final ResponseEntity<byte[]> responseEntity
                = retryRestTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

        final Integer httpStatus = responseEntity.getStatusCode().value();

        return new ResponseDTO(responseEntity.getBody(), responseEntity.getHeaders().toSingleValueMap(), httpStatus);
    }

    @Override
    public ResponseDTO getDataFromUUID(final String uuid) throws IOException {
        final String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_DATA_FROM_UUID + "/" + uuid;
        URLConnection urlConnection = new URL(url).openConnection();
        final Map<String, List<String>> headers = urlConnection.getHeaderFields();
        final InputStream inputStream = urlConnection.getInputStream();
        final Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(ResponseDTO.CONTENT_TYPE, headers.get(ResponseDTO.CONTENT_TYPE).get(0));
        return new ResponseDTO(inputStream, responseHeaders);
    }

    @Override
    public String getParentResource(final String uuid) throws ResourceNotFound {
        String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_PARENT_UUID;
        LOG.trace("API call: " + url);
        LOG.trace("Request of parent uuid of: " + uuid);
        String ret = retryRestTemplate.postForObject(url, uuid, String.class);
        LOG.trace("Response: {}", ret);
        return ret;
    }

    @Override
    public URL getDataURL(final String uri) throws IOException {
        final String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_DATA + "/" + uri;
        return new URL(url);
    }

    @Override
    public void cacheResource(CacheDTO cacheDTO) throws DataNotFound, CacheValidationException {
        nullValidationChecker(cacheDTO, CacheDTO.class);
        boolean isNotValidObject = cacheDTO.getCacheKey() == null || (cacheDTO.getCacheBlobBinary() == null && cacheDTO.getCacheBlobURI() == null);
        if (isNotValidObject) {
            throw new CacheValidationException("CacheDTO object is missing mandatory fields");
        } else {
            StringBuffer url = new StringBuffer(getUrlRoot()).append(Constants.PUT_CACHE);
            LOG.debug("API call: " + url);
            LOG.debug("Request DTO: " + cacheDTO.toString());
            retryRestTemplate.postForObject(url.toString(), cacheDTO, CacheDTO.class);
        }
    }

    @Override
    public void removeCachedResource(RemoveCacheDTO removeCacheDTO) throws DataNotFound, CacheValidationException {
        nullValidationChecker(removeCacheDTO, RemoveCacheDTO.class);
        boolean isNotValidObject = removeCacheDTO.getCacheKey() == null;
        if (isNotValidObject) {
            throw new CacheValidationException("RemoveCacheDTO object is missing the cacheKey");
        } else {
            StringBuffer url = new StringBuffer(getUrlRoot()).append(Constants.REMOVE_CACHE);
            LOG.debug("API call: " + url);
            LOG.debug("Request DTO: " + removeCacheDTO.toString());
            retryRestTemplate.postForObject(url.toString(), removeCacheDTO, RemoveCacheDTO.class);
        }
    }

    @Override
    public CacheDTO getCachedResource(QueryCacheDTO queryCacheDTO) throws DataNotFound, CacheValidationException {
        nullValidationChecker(queryCacheDTO, QueryCacheDTO.class);
        boolean isNotValidObject = queryCacheDTO.getCacheKey() == null;
        CacheDTO cacheDTO = null;
        if (isNotValidObject) {
            throw new CacheValidationException("QueryCacheDTO object is missing the cacheKey");
        } else {
            if (hazelcastInstance.isPresent()) {
                final String cache = StringUtils.isEmpty(queryCacheDTO.getCacheRegion()) ? Constants.DEFAULT_CACHE_NAME : queryCacheDTO.getCacheRegion();
                LOG.debug("Trying to find resource ({},{}) in local HZ instance", queryCacheDTO.getCacheKey(), cache);
                if (hazelcastInstance.get().getMap(cache).containsKey(queryCacheDTO.getCacheKey())) {
                    try {
                        cacheDTO = (CacheDTO) hazelcastInstance.get().getMap(cache).get(queryCacheDTO.getCacheKey());
                        if (cacheDTO.getCacheExpirationTime().isBeforeNow()) {
                            final RemoveCacheDTO removeCacheDTO = new RemoveCacheDTO();
                            removeCacheDTO.setCacheKey(queryCacheDTO.getCacheKey());
                            removeCacheDTO.setCacheRegion(queryCacheDTO.getCacheRegion());
                            removeCachedResource(removeCacheDTO);
                        } else {
                            LOG.debug("Resource ({},{}) found in local HZ instance", queryCacheDTO.getCacheKey(), cache);
                            return cacheDTO;
                        }
                    } catch (Exception e) {
                        LOG.debug("QueryCacheDTO with key={} was not found in region={}", queryCacheDTO.getCacheKey(), queryCacheDTO.getCacheRegion());
                    }
                }
            }

            StringBuffer url = new StringBuffer(getUrlRoot()).append(Constants.GET_CACHE);
            LOG.debug("API call: " + url);
            LOG.debug("Request DTO: " + queryCacheDTO.toString());
            LOG.trace("API call: " + url);
            cacheDTO = retryRestTemplate.postForObject(url.toString(), queryCacheDTO, CacheDTO.class);

            LOG.debug("Response DTO: {}", cacheDTO);

        }
        return cacheDTO;
    }

    @Override
    public void clearCache(RemoveCacheDTO removeCacheDTO) throws DataNotFound, CacheValidationException {
        nullValidationChecker(removeCacheDTO, RemoveCacheDTO.class);
        StringBuffer url = new StringBuffer(getUrlRoot()).append(Constants.CLEAR_CACHE_REGION);
        LOG.debug("API call: " + url);
        if (removeCacheDTO != null)
            LOG.debug("Request DTO: " + removeCacheDTO.toString());
        retryRestTemplate.postForObject(url.toString(), removeCacheDTO, RemoveCacheDTO.class);
    }

    @Override
    public void clearCache() {
        StringBuffer url = new StringBuffer(getUrlRoot()).append(Constants.CLEAR_CACHE_ALL);
        LOG.debug("API call: " + url);
        retryRestTemplate.postForObject(url.toString(), true, Boolean.class);
    }

    @Override
    public void clearCacheExcept(List<String> cacheRegions) {
        StringBuffer url = new StringBuffer(getUrlRoot()).append(Constants.CLEAR_CACHE_ALL_EXCEPT);
        LOG.debug("API call: " + url);
        retryRestTemplate.postForObject(url.toString(), cacheRegions, List.class);
    }

    @Override
    public String getUID(String region) {
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.GET_UID);
        LOG.debug("API call: " + url);
        if(StringUtils.isEmpty(region)){
            region = Constants.UID_REGION;
        }
        final String UID = retryRestTemplate.postForObject(url.toString(), region, String.class);
        return UID;
    }

    @Override
    public String getUID() {
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.GET_UID);
        LOG.debug("API call: " + url);
        final String UID = retryRestTemplate.postForObject(url.toString(), Constants.UID_REGION, String.class);
        return UID;
    }

    @Override
    public LockDTO lockResource(NewLockDTO newLockDTO) throws LockNotAllowed,LockValidationException {
        nullValidationChecker(newLockDTO,NewLockDTO.class);
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.LOCK_RESOURCE);
        LOG.debug("API call: " + url);
        LOG.debug("NewLockDTO: " + newLockDTO.toString());
        LockDTO lockDTO = retryRestTemplate.postForObject(url.toString(), newLockDTO, LockDTO.class);
        return lockDTO;
    }

    @Override
    public void unlockResource(LockDTO lockDTO) throws LockNotHeld,LockValidationException{
        nullValidationChecker(lockDTO,LockDTO.class);
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.UNLOCK_RESOURCE);
        LOG.debug("API call: " + url);
        LOG.debug("LockDTO: " + lockDTO.toString());
        retryRestTemplate.postForObject(url.toString(), lockDTO, LockDTO.class);
    }

    @Override
    public LockDTO renewResourceLock(LockDTO lockDTO) throws LockNotAllowed, LockNotHeld,LockValidationException {
        nullValidationChecker(lockDTO,LockDTO.class);
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.RENEW_RESOURCE_LOCK);
        LOG.debug("API call: " + url);
        LOG.debug("LockDTO: " + lockDTO.toString());
        LockDTO retLockDTO = retryRestTemplate.postForObject(url.toString(), lockDTO, LockDTO.class);
        return retLockDTO;
    }

    @Override
    public LockDTO queryResourceLock(QueryLockDTO queryLockDTO) throws LockNotFound, LockValidationException{
        nullValidationChecker(queryLockDTO,QueryLockDTO.class);
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.QUERY_RESOURCE_LOCK);
        LOG.debug("API call: " + url);
        LOG.debug("QueryDTO: " + queryLockDTO.toString());
        LockDTO retLockDTO = retryRestTemplate.postForObject(url.toString(), queryLockDTO, LockDTO.class);
        return retLockDTO;
    }

    @Override
    public List<ResourceDTO> queryAllRes(){
        StringBuilder url = new StringBuilder(getUrlRoot()).append(Constants.GET_ALL_RESOURCES);
        LOG.trace("API call: " + url);
        List<ResourceDTO> retList = retryRestTemplate.getForObject(url.toString(),List.class);
        return retList;
    }

    @Override
    public List<String[]> queryAllRevs(String uid){
        final String url = apiVersionClient.getApiUrl() + "/" + Constants.GET_ALL_REVISIONS + "/" + uid;
        LOG.trace("API call: " + url);
        List<String[]> retList=retryRestTemplate.getForObject(url.toString(),List.class,uid);
        return retList;
    }

    private void nullValidationChecker(Object obj, Class aClass) {
        if (obj == null) {
            throw new CacheValidationException(aClass + " object cannot be null!");
        }
    }
}
