## 📊💿 Custom sharepoint list adapter for SAP HANA

Support standalone versions only.

Beta version, for non-production usage only.

## Example
<img width=90% alt="Screenshot 2020-10-30 at 08 58 52" src="https://user-images.githubusercontent.com/11619019/97712759-2cf10200-1abf-11eb-9350-80e6a644aba5.png">
<img width=60% alt="Screenshot 2020-10-30 at 09 12 34" src="https://user-images.githubusercontent.com/11619019/97712689-12b72400-1abf-11eb-9be3-391116088553.png">
<img width=90% alt="Screenshot 2020-10-30 at 12 30 14" src="https://user-images.githubusercontent.com/11619019/97712725-21054000-1abf-11eb-9337-dc853092c5d6.png">

## How it works ?

oData standard sharepoint API: https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/working-with-lists-and-list-items-with-rest
and standard JAVA library to handle NTLM login.

### Usage

1.  Download [latest release](https://github.com/pawelwiejkut/hana_sharepoint_list_adapter/releases).
2.  Deploy the adapter (.jar) to the Data Provisioning Agent
3.  Register the adapter with the SAP HANA server
4.  Enable the adapter in SAP HANA studio (create a new remote source and import tables)
5.  Usefull links:

How to create your own: https://pawelwiejkut.dev/posts/custom-hana-adapter/

SAP Documentation: https://help.sap.com/doc/PRODUCTION/a886342618b84c7e84b81adce1a1346b/2.0_SPS00/en-US/SAP_HANA_EIM_Adapter_SDK_en.pdf
