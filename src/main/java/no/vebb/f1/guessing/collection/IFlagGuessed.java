package no.vebb.f1.guessing.collection;

import no.vebb.f1.stats.domain.Flag;

public interface IFlagGuessed {
    Flag getFlagName();
    int getGuessed();
    int getActual();
}
