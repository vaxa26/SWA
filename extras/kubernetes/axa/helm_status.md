## helm status

display the status of the named release

### Synopsis


This command shows the status of a named release.
The status consists of:
- last deployment time
- k8s namespace in which the release lives
- state of the release (can be: unknown, deployed, uninstalled, superseded, failed, uninstalling, pending-install, pending-upgrade or pending-rollback)
- revision of the release
- description of the release (can be completion message or error message, need to enable --show-desc)
- list of resources that this release consists of (need to enable --show-resources)
- details on last test suite run, if applicable
- additional notes provided by the chart


```
helm status RELEASE_NAME [flags]
```

### Options

```
  -h, --help             help for status
  -o, --output format    prints the output in the specified format. Allowed values: table, json, yaml (default table)
      --revision int     if set, display the status of the named release with revision
      --show-desc        if set, display the description message of the named release
      --show-resources   if set, display the resources of the named release
```

### Options inherited from parent commands

```
      --burst-limit int                 client-side default throttling limit (default 100)
      --debug                           enable verbose output
      --kube-apiserver string           the address and the port for the Kubernetes API server
      --kube-as-group stringArray       group to impersonate for the operation, this flag can be repeated to specify multiple groups.
      --kube-as-user string             username to impersonate for the operation
      --kube-ca-file string             the certificate authority file for the Kubernetes API server connection
      --kube-context string             name of the kubeconfig context to use
      --kube-insecure-skip-tls-verify   if true, the Kubernetes API server's certificate will not be checked for validity. This will make your HTTPS connections insecure
      --kube-tls-server-name string     server name to use for Kubernetes API server certificate validation. If it is not provided, the hostname used to contact the server is used
      --kube-token string               bearer token used for authentication
      --kubeconfig string               path to the kubeconfig file
  -n, --namespace string                namespace scope for this request
      --qps float32                     queries per second used when communicating with the Kubernetes API, not including bursting
      --registry-config string          path to the registry config file (default "C:\\Users\\vieta\\AppData\\Roaming\\helm\\registry\\config.json")
      --repository-cache string         path to the file containing cached repository indexes (default "C:\\Users\\vieta\\AppData\\Local\\Temp\\helm\\repository")
      --repository-config string        path to the file containing repository names and URLs (default "C:\\Users\\vieta\\AppData\\Roaming\\helm\\repositories.yaml")
```

### SEE ALSO

* [helm](helm.md)	 - The Helm package manager for Kubernetes.

###### Auto generated by spf13/cobra on 3-May-2024