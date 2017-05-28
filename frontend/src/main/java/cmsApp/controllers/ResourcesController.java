package cmsApp.controllers;

import com.sastix.cms.common.content.CreateResourceDTO;
import com.sastix.cms.common.content.LockedResourceDTO;
import com.sastix.cms.common.content.ResourceDTO;
import com.sastix.cms.common.content.UpdateResourceDTO;
import cmsApp.helpers.Bucket;
import cmsApp.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by giwrgos on 15/5/2017.
 */
@Controller
public class ResourcesController {

    private ResourceService resourceService;

    @Autowired
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @RequestMapping(value = "/resources", method = RequestMethod.GET)
    public String allResources(Model model) {
        model.addAttribute("resources", resourceService.getAllRes());
        System.out.println("Returning resources:");
        return "resources";
    }

    @RequestMapping("/resource/new")
    public String newResource(Model model){
        model.addAttribute("resource",new CreateResourceDTO());
        return "resourceform";
    }

    @RequestMapping(value="resource/delete/{resourceUID}",method=RequestMethod.GET)
    public String deleteResource(@PathVariable("resourceUID") String resourceUID){
        ResourceDTO retRes = resourceService.getRes(resourceUID);
        LockedResourceDTO lockedResourceDTO= resourceService.lockResource(retRes);

        ResourceDTO resourceDTO =resourceService.deleteResource(lockedResourceDTO);
        return "redirect:/";

    }

    @RequestMapping(value="resource/edit/{resourceUID}",method=RequestMethod.GET)
    public String modifyResource(@PathVariable("resourceUID") String resourceUID,Model model){
        ResourceDTO retRes = resourceService.getRes(resourceUID);
        Bucket retBucket = new Bucket();
        retBucket.setUid(resourceUID);
        retBucket.setAuthor(retRes.getAuthor());
        retBucket.setLockOwn(retRes.getAuthor());
        model.addAttribute("bucket", retBucket);
        return "resourceupdateform";
    }

    @RequestMapping(value = "resource", method = RequestMethod.POST)
    public String saveResource(CreateResourceDTO resourceDTO){
       resourceService.createRes(resourceDTO);
        return "redirect:/";
    }

    @RequestMapping(value = "resource/save", method = RequestMethod.POST)
    public String updateResource(Bucket updatebucket){
        System.out.println("ENTERED UPDATE RESOURCE");
        ResourceDTO retRes = resourceService.getRes(updatebucket.getUid());
        LockedResourceDTO lockedResourceDTO= resourceService.lockResource(retRes);
        UpdateResourceDTO updateResourceDTO = new UpdateResourceDTO();
        updateResourceDTO.setResourceAuthor(updatebucket.getAuthor());
        updateResourceDTO.setAuthor(updatebucket.getLockOwn());
        updateResourceDTO.setResourceExternalURI(updatebucket.getUri());
        updateResourceDTO.setResourceName(updatebucket.getName());
        updateResourceDTO.setResourceUID(updatebucket.getUid());
        updateResourceDTO.setLockExpirationDate(lockedResourceDTO.getLockExpirationDate());
        updateResourceDTO.setLockID(lockedResourceDTO.getLockID());
        resourceService.updateResource(updateResourceDTO);
        resourceService.unlockResource(lockedResourceDTO);
        return "redirect:/";
    }

    @RequestMapping(value="resource/revisions/{resourceUID}",method=RequestMethod.GET)
    public String getRevisions(@PathVariable("resourceUID") String resourceUID,Model model){
        model.addAttribute("revisions", resourceService.getAllRevs(resourceUID));
        System.out.println("Returning revisions:");
        return "resourceRevisions";
    }

}
