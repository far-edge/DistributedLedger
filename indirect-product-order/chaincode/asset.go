package main
	
type OrderCommand struct {
	FirstName        string `json:"firstName"`
	LastName         string `json:"lastName"`
	Title            string `json:"title"`
	Company          string `json:"company"`
	Email            string `json:"email"`
	Phone            string `json:"phone"`
	InternalOrder    string `json:"internalOrder"`
	InternalCustomer string `json:"internalCustomer"`
	Order            string `json:"order"`
	Customer         string `json:"customer"`
	Color            []int  `json:"color"`
	Simulate         []int  `json:"simulate"`
	Giveaway         []int  `json:"giveaway"`
	Priority         int    `json:"priority"`
	Type             string `json:"type"`
	Operation 		 string `json:"operation"`
 }