package com.jacrispys.JavaBot.Utils.ExperimentalUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.WARNING;

@SupportedAnnotationTypes("ExperimentalUtils.Experimental")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ExperimentalProcessor extends AbstractProcessor {

    private ProcessingEnvironment env;

    @Override
    public synchronized void init(ProcessingEnvironment pe) {
        this.env = pe;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            for (TypeElement te : annotations) {
                final Set< ? extends Element> elts = roundEnv.getElementsAnnotatedWith(te);
                for (Element elt : elts) {
                    env.getMessager().printMessage(WARNING,
                            String.format("%s : this method is experimental! %s", roundEnv.getRootElements(), elt),
                            elt);
                }
            }
        }
        return true;
    }
}
