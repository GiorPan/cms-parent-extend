package cmsApp.services;

import com.sastix.cms.client.impl.CmsClient;
import com.sastix.cms.common.lock.LockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import com.sastix.cms.common.content.*;

/**
 * Created by giwrgos on 16/5/2017.
 */
@Service
@ComponentScan("com.sastix.cms.client.impl")
public class ResourceService {



    @Autowired
    private CmsClient client;

    public ResourceDTO createRes(CreateResourceDTO resourceDTO){
        System.out.println("Entered create Resource");
        ResourceDTO resCreated= this.client.createResource(resourceDTO);
        System.out.println("created resource with uid "+ resCreated.getResourceUID());
        return resCreated;
    }



    public ResourceDTO getRes(String resUid){
        System.out.println("asdasdas"+resUid);
        ResourceQueryDTO resQuer=new  ResourceQueryDTO(resUid);
        ResourceDTO retRes=client.queryResource(resQuer);
        return retRes;

    }

    public Iterable<ResourceDTO> getAllRes(){

        return client.queryAllRes();
    }

    public Iterable<String[]> getAllRevs(String uid){

        return this.client.queryAllRevs(uid);
    }

    public ResourceDTO deleteResource(LockedResourceDTO lockedResourceDTO){
        return this.client.deleteResource(lockedResourceDTO);
    }

    public LockedResourceDTO updateResource(UpdateResourceDTO updateResourceDTO){

        return client.updateResource(updateResourceDTO);
    }

    public LockedResourceDTO lockResource(ResourceDTO resourceDTO){

        return client.lockResource(resourceDTO);
    }

    public void unlockResource(LockDTO lockDTO){
        client.unlockResource(lockDTO);
        return;
    }
    public void unlockResource(LockedResourceDTO lockedResourceDTO){
        client.unlockResource(lockedResourceDTO);
        return;
    }


}
