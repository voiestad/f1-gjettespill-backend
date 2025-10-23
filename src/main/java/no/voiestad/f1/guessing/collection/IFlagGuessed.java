package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.stats.domain.Flag;

public interface IFlagGuessed {
    Flag getFlagName();
    int getGuessed();
    int getActual();
}
