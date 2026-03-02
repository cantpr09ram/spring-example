controller 是 HTTP 入口。只做三件事：接 request、驗證參數、呼叫 service。不要寫商業邏輯。
service 放真正的邏輯，例如計算、流程控制、交易處理。未來 70% 的程式會在這層。
repository 是資料庫存取層，通常繼承 JpaRepository。controller 不應直接碰它。
entity 是資料庫 table 對應物件（JPA）。
dto 是 API 對外輸出的資料格式。實務上幾乎不直接回 entity。
config 放 Spring 設定，例如 CORS、Security、Bean。
exception 用來集中處理錯誤，而不是每個 controller try/catch。