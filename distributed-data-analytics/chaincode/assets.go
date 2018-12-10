package main

type AnalyticsInstances struct {
	Id                     string `json:"id"`
	Name                   string `json:"name"`
	EdgeGatewayReferenceID string `json:"edgeGatewayReferenceID"`
	Payload                string `json:"payload"`
	Type                   string `json:"type"`
}

type DataSource struct {
	Id                              string `json:"id"`
	Name                            string `json:"name"`
	EdgeGatewayReferenceID          string `json:"edgeGatewayReferenceID"`
	DataSourceDefinitionReferenceID string `json:"dataSourceDefinitionReferenceID"`
	Payload                         string `json:"payload"`
	Type                            string `json:"type"`
}

type EdgeGateway struct {
	Id         string `json:"id"`
	Name       string `json:"name"`
	Namespace  string `json:"namespace"`
	MacAddress string `json:"macAddress"`
	Payload    string `json:"payload"`
	Type       string `json:"type"`
}
