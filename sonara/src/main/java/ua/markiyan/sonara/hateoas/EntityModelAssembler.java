package ua.markiyan.sonara.hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;


public interface EntityModelAssembler<T> extends RepresentationModelAssembler<T, EntityModel<T>> {
}

