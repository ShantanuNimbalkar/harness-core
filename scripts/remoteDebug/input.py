import os

service = input("Enter the service name ")
print(service)

variables = {"pipeline-service": ["bt-test", "pipeline-service", "7890", "5005",
                                  "~/.bazel-dirs/bin/pipeline-service/service/module_deploy"
                                  ".jar", "pipeline-service-capsule.jar"]}

os.system("sh remote-debug.sh " + " ".join(variables[service]))
