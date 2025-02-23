package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Controller;
import net.theevilreaper.vulpes.api.model.ItemModel;
import net.theevilreaper.vulpes.api.repository.ItemRepository;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@Controller("/item")
public class ItemController {

    private final ItemRepository itemRepository;

    @Inject
    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModel> add(@Body ItemModel item) {
        return HttpResponse.ok(itemRepository.save(item));
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModel> getById(@PathVariable String id) {
        Optional<ItemModel> model = itemRepository.findById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

    @Delete("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModel> remove(@PathVariable String id) {
        Optional<ItemModel> model = itemRepository.findById(id);
        if (model.isPresent()) {
            itemRepository.deleteById(id);
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<ItemModel>> getAll() {
        return HttpResponse.ok(itemRepository.findAll());
    }

    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<ItemModel>> deleteAll() {
        itemRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModel> update(@Body ItemModel model) {
        return HttpResponse.ok(itemRepository.update(model));
    }
}