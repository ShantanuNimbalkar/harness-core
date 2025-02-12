load("@rules_spring//springboot:springboot.bzl", "springboot")

template = """
package test.duplicated;

public class MainClass {
    public static void main(String[] args) {
    }
}
"""

def report_duplicated(**kwargs):
    native.genrule(
        name = "test_duplicated_main",
        outs = ["test_duplicate_output/duplicated/MainClass.java"],
        cmd = """
cat <<EOF >> $@
%s
EOF""" % template,
        tags = ["manual", "no-ide", "analysis", "duplicated"],
    )

    native.java_library(
        name = "test_duplicated_library",
        srcs = ["test_duplicated_main"],
        visibility = ["//visibility:private"],
        deps = [":module", "@maven//:org_springframework_boot_spring_boot_loader"],
        tags = ["manual", "no-ide", "analysis", "duplicated"],
    )

    springboot(
        name = "test_duplicated",
        boot_app_class = "io.harness.delegate.app.DelegateApplication",
        java_library = ":test_duplicated_library",
        visibility = ["//visibility:public"],
        dupeclassescheck_enable = True,
        tags = ["manual", "no-ide", "analysis", "duplicated"],
    )
