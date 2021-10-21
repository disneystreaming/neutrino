package com.disneystreaming.neutrino.annotation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.lang.annotation.Annotation;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class ConcreteNestedAnnotation implements NestedAnnotation, Serializable {
    private final Annotation innerAnnotation;
    private final Class<? extends Annotation> innerAnnotationType;

    @Override
    public Class<? extends Annotation> annotationType() {
        return NestedAnnotation.class;
    }
}
