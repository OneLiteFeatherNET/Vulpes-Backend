package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import net.theevilreaper.vulpes.api.model.AttributeModel;
import net.theevilreaper.vulpes.api.repository.AttributeRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import net.theevilreaper.vulpes.backend.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Controller("/attribute")
public class AttributeController {

    private final AttributeRepository attributeRepository;

    @Inject
    public AttributeController(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    @Post
    public HttpResponse<AttributeModel> add(AttributeModel model) {
        return HttpResponse.ok(attributeRepository.save(model));
    }

    @Post("/update")
    public HttpResponse<AttributeModel> update(@Body AttributeModel model) {
        return HttpResponse.ok(attributeRepository.save(model));
    }

    //TODO: Path variable??
    @Delete("/delete/{id}")
    public HttpResponse<AttributeModel> delete(String id) {
        Optional<AttributeModel> attributeModel = attributeRepository.findById(id);
        if (attributeModel.isPresent()) {
            attributeRepository.deleteById(id);
            return HttpResponse.ok(attributeModel.get());
        }
        throw new ResourceNotFoundException(HttpMethod.DELETE, id);
    }

    @Delete("/deleteAll")
    public HttpResponse<List<AttributeModel>> deleteAll() {
        attributeRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    /**
     * Returns all [AttributeModel] which are currently persists in the database.
     *
     * @return a list with all [AttributeModel] mapped in a [HttpResponse]
     */
    @Get("/getAll")
    public HttpResponse<List<AttributeModel>> getAll() {
        return HttpResponse.ok(attributeRepository.findAll());
    }
}
