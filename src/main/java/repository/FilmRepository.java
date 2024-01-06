package repository;

import com.speedment.jpastreamer.application.JPAStreamer;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.streamconfiguration.StreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.quarkustutorial.app.model.FilmEntity;
import org.quarkustutorial.app.model.FilmEntity$;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class FilmRepository {

    @Inject
    JPAStreamer jpaStreamer;

    private static final int PAGE_SIZE = 20;

    public Optional<FilmEntity> getFilm(short filmId){
        return jpaStreamer.stream(FilmEntity.class)
                .filter(FilmEntity$.filmId.equal(filmId))
                .findFirst();
    }

    public Stream<FilmEntity> getFilmsWithPrice(short minLength){
        return jpaStreamer.stream(FilmEntity.class)
                .filter(FilmEntity$.filmId.greaterThan(minLength))
                .sorted(FilmEntity$.length);
    }

    public Stream<FilmEntity> paged(long page, short minLength) {
        return jpaStreamer.stream(Projection.select(FilmEntity$.filmId, FilmEntity$.title, FilmEntity$.length))
                .filter(FilmEntity$.length.greaterThan(minLength))
                .sorted(FilmEntity$.length)
                .skip(page*PAGE_SIZE)
                .limit(PAGE_SIZE);
    }

    public Stream<FilmEntity> actors(String startsWith, short minLenght) {
        final StreamConfiguration<FilmEntity> joining =
                StreamConfiguration.of(FilmEntity.class).joining(FilmEntity$.actors);
        return jpaStreamer.stream(joining)
                .filter(FilmEntity$.title.startsWith(startsWith).and(FilmEntity$.length.greaterThan(minLenght)))
                .sorted(FilmEntity$.length.reversed());
    }

    @Transactional
    public void updateRentalRate(short minLength, BigDecimal rentalRate) {
        jpaStreamer.stream(FilmEntity.class)
                .filter(FilmEntity$.length.greaterThan(minLength))
                .forEach(f -> {
                    f.setRentalRate(rentalRate);
                });
    }
}
