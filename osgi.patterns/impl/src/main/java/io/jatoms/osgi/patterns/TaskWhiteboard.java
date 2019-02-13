package io.jatoms.osgi.patterns;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.jatoms.osgi.patterns.api.ITask;

@Component
public class TaskWhiteboard {

    @Reference(policyOption=ReferencePolicyOption.GREEDY)
    private List<ITask> tasks = new CopyOnWriteArrayList<>();

    

    @Activate
    void activate () {

    }


}