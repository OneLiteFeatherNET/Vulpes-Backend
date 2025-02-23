package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import jakarta.inject.Inject;
import net.theevilreaper.vulpes.api.model.FontModel;
import net.theevilreaper.vulpes.api.repository.FontRepository;

import java.util.List;
import java.util.Optional;

@Controller("/font")
public class FontController {

    private final FontRepository fontRepository;

    @Inject
    public FontController(FontRepository fontRepository) {
        this.fontRepository = fontRepository;
    }

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModel> add(@Body FontModel item) {
        return HttpResponse.ok(fontRepository.save(item));
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModel> getById(@PathVariable String id) {
        Optional<FontModel> model = fontRepository.findById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

    @Delete("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModel> remove(@PathVariable String id) {
        Optional<FontModel> model = fontRepository.findById(id);
        if (model.isPresent()) {
            fontRepository.deleteById(id);
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontModel>> getAll() {
        return HttpResponse.ok(fontRepository.findAll());
    }

    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontModel>> deleteAll() {
        fontRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModel> update(@Body FontModel model) {
        return HttpResponse.ok(fontRepository.update(model));
    }
}
