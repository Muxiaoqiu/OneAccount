package butterknife;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ButterKnife {

    private ButterKnife() {}

    public static Unbinder bind(final Activity target) {
        return bind(target, target.getWindow().getDecorView());
    }

    public static Unbinder bind(final Object target, final View source) {
        injectViews(target, source);
        injectClicks(target, source);
        return new Unbinder() {
            @Override public void unbind() {}
        };
    }

    private static void injectViews(Object target, View source) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                BindView bindView = field.getAnnotation(BindView.class);
                if (bindView != null) {
                    try {
                        field.setAccessible(true);
                        field.set(target, source.findViewById(bindView.value()));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to bind view for field: " + field.getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static void injectClicks(final Object target, final View source) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            for (final Method method : clazz.getDeclaredMethods()) {
                OnClick onClick = method.getAnnotation(OnClick.class);
                if (onClick != null) {
                    for (int id : onClick.value()) {
                        View view = source.findViewById(id);
                        if (view != null) {
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        method.setAccessible(true);
                                        method.invoke(target, v);
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failed to invoke @OnClick method: " + method.getName(), e);
                                    }
                                }
                            });
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
