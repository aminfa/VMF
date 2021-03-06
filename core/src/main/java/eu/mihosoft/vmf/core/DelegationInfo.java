/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 * Copyright 2017-2019 Goethe Center for Scientific Computing, University Frankfurt. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */
package eu.mihosoft.vmf.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>Created by miho on 21.03.2017.</p>
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@Deprecated
public class DelegationInfo {
    private final String fullTypeName;
    private final String returnType;
    private final String methodName;
    private final List<String> paramTypes;
    private final List<String> paramNames;

    private static final List<String> delegationTypes = new ArrayList<>();

    private final String varName;

    private final boolean constructor;

    private DelegationInfo(String fullTypeName, String methodName, String returnType, List<String> paramTypes, List<String> paramNames, boolean constructor) {
        this.fullTypeName = fullTypeName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.paramTypes = Collections.unmodifiableList(paramTypes);
        this.paramNames = Collections.unmodifiableList(paramNames);

        if(!delegationTypes.contains(fullTypeName)) {
            delegationTypes.add(fullTypeName);
        }

        varName = "__vmf_delegate_"+delegationTypes.indexOf(fullTypeName);

        this.constructor = constructor;
    }

    public static DelegationInfo newInstance(String className, String methodName, String returnType, List<String> paramTypes, List<String> paramNames, boolean constructor) {
        return new DelegationInfo(className, methodName, returnType, paramTypes, paramNames, constructor);
    }

    public static DelegationInfo newInstance(Model model, Method m) {
        DelegateTo delegation = m.getAnnotation(DelegateTo.class);

        if(delegation==null) {
            return null;
        }

        List<String> paramTypes = new ArrayList<>(m.getParameters().length);
        List<String> paramNames = new ArrayList<>(m.getParameters().length);

        for(Parameter p : m.getParameters()) {
            paramTypes.add(TypeUtil.getTypeAsString(model,p.getType()));
            paramNames.add(p.getName());
        }

        return newInstance(
                delegation.className(),
                m.getName(),
                TypeUtil.getReturnTypeAsString(model,m),
                paramTypes, paramNames, false);
    }

    public static DelegationInfo newInstance(Model model, Class<?> clazz) {

        DelegateTo delegation = clazz.getAnnotation(DelegateTo.class);

        if(delegation==null) {
            return null;
        }

        List<String> paramTypes = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();


        return newInstance(
                delegation.className(),
                "on"+clazz.getSimpleName()+"Instantiated",
                TypeUtil.getTypeAsString(model,clazz),
                paramTypes, paramNames, true);
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public String getFullTypeName() {
        return fullTypeName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }


    public String getMethodDeclaration() {
        String method = "public " + getReturnType() + " " + getMethodName()+"(";

        for(int i = 0; i < paramTypes.size();i++) {
            method += (i>0?", ":"") + paramTypes.get(i) + " " + paramNames.get(i);
        }

        method+=")";

        return method;
    }

    public boolean isVoid() {
        return returnType.equals(void.class.getName());
    }

    public boolean isConstructor() {
        return constructor;
    }

    public String getVarName() {
        return varName;
    }

}
