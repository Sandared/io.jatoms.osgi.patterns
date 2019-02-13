package io.jatoms.itask.simple;

import org.osgi.service.component.annotations.Component;

import io.jatoms.osgi.patterns.api.ITask;

@Component
public class ComponentImpl implements ITask{
	@Override
	public void run() {
        System.out.println("Hello Whiteboard!");
	}
}
