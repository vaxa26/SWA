{{/*
Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
#
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Anpassungen:
- ServiceAccount wurde entfernt und wird explizit definiert
- Zusaetzliche Labels gemaess Kubescape
*/}}
{{/*
Expand the name of the chart.
*/}}
{{- define "axa.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "axa.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "axa.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels
https://kubernetes.io/docs/reference/labels-annotations-taints
https://helm.sh/docs/chart_best_practices/labels/#standard-labels
https://hub.armosec.io/docs/configuration_parameter_recommendedlabels
*/}}
{{- define "axa.labels" -}}
helm.sh/chart: {{ include "axa.chart" . }}
{{ include "axa.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: acme
{{- end }}

{{/*
Selector labels
*/}}
{{- define "axa.selectorLabels" -}}
app: {{ include "axa.name" . }}
app.kubernetes.io/name: {{ include "axa.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
