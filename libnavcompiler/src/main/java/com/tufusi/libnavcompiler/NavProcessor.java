package com.tufusi.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.tufusi.libnavannotation.ActivityDestination;
import com.tufusi.libnavannotation.FragmentDestination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Created by 鼠夏目 on 2020/9/21.
 *
 * @author 鼠夏目
 * @description APP页面导航信息收集注解处理器
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.tufusi.libnavannotation.ActivityDestination", "com.tufusi.libnavannotation.FragmentDestination"})
public class NavProcessor extends AbstractProcessor {

    private static final String OUTPUT_FILE_NAME = "dest.json";

    private Messager mMessager;
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> actElements = roundEnv.getElementsAnnotatedWith(ActivityDestination.class);
        Set<? extends Element> fragElements = roundEnv.getElementsAnnotatedWith(FragmentDestination.class);

        if (!actElements.isEmpty() || !fragElements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handlerDestination(actElements, ActivityDestination.class, destMap);
            handlerDestination(fragElements, FragmentDestination.class, destMap);

            FileOutputStream fos = null;
            OutputStreamWriter writer = null;
            try {
                FileObject resource = mFiler.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);

                // 此位置 //app/build/intermediates/javac/debug/classes
                String resourcePath = resource.toUri().getPath();
                mMessager.printMessage(Diagnostic.Kind.NOTE, "resourcePath:" + resourcePath);

                // 定位到 //app/src/main/assets
                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
                String assetsPath = appPath + "src/main/assets";

                // 写入文件
                File file = new File(assetsPath);
                if (!file.exists()) {
                    file.mkdirs();
                }

                File outputFile = new File(file, OUTPUT_FILE_NAME);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                outputFile.createNewFile();

                String content = JSON.toJSONString(destMap);
                fos = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(fos, "UTF-8");
                writer.write(content);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    private void handlerDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClz, HashMap<String, JSONObject> destMap) {
        // 由于标记在类上，因此可以直接转成 TypeElement
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;

            String className = typeElement.getQualifiedName().toString();
            int id = Math.abs(annotationClz.hashCode());
            String pageUrl = null;
            boolean needLogin = false;
            boolean asStarter = false;
            boolean isFragment = false;

            // 获取注解
            Annotation annotation = typeElement.getAnnotation(annotationClz);
            if (annotation instanceof ActivityDestination) {
                ActivityDestination dest = (ActivityDestination) annotation;

                pageUrl = dest.pageUrl();
                needLogin = dest.needLogin();
                asStarter = dest.asStarter();
                isFragment = false;
            } else if (annotation instanceof FragmentDestination) {
                FragmentDestination dest = (FragmentDestination) annotation;

                pageUrl = dest.pageUrl();
                needLogin = dest.needLogin();
                asStarter = dest.asStarter();
                isFragment = true;
            }

            if (destMap.containsKey(pageUrl)) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "Different pages cannot contain the same pageUrl: " + className);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", id);
                jsonObject.put("className", className);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("needLogin", needLogin);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("isFragment", isFragment);
                destMap.put(pageUrl, jsonObject);
            }
        }
    }
}