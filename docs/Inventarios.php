<?php

namespace App\Controllers;
require(ROOTPATH.'vendor/autoload.php');

use App\Controllers\BaseController;
use App\Models\HistorialProblemasMdl;
use App\Models\InventariosMdl;
use App\Models\UbicacionesMdl;
use App\Models\InspeccionesDetMdl;
use App\Models\ProblemasMdl;
use App\Models\LineaBaseMdl;
use App\Models\FabricantesMdl;
use App\Models\TipoPrioridadMdl;
use App\Models\EstatusInspecDetMdl;
use App\Models\InspeccionesMdl;
use App\Models\UsuariosMdl;
use App\Models\SitiosMdl;
use App\Models\FotosProblemasMdl;
use App\Models\DatosReporteMdl;
use App\ThirdParty\fpdf\FPDF;
use App\ThirdParty\fpdi\src\Fpdi;


use PhpOffice\PhpSpreadsheet\Spreadsheet;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx;

class Inventarios extends BaseController{

    public function index(){
        $inspeccionesMdl = new InspeccionesMdl();
        $session = session();
        // Si no se ha iniciado session redirecciona al login
        if(is_null($session->usuario) || $session->usuario == ''){
            $session->setFlashdata('msg', 'Es necesario iniciar sesión');
            return redirect()->to(base_url('/'));
        }

        // var_dump($session);
        /* Si hay una inpeccion aun en progreso , volver a abrirla automaticamnete
        esto en caso de que la inspeccion se lleve a cabo en mas de un dia para no volver a reestableces la BD y perder datos */

        // si no hay una inspeccionabierta se busca la ultima inspeccion existente y se abre
        if(is_null($session->Id_Inspeccion) || $session->Id_Inspeccion == ""){
            // Obteneindo los datos de la inspeccion restaurada
            $datos_inspeccion = $inspeccionesMdl->datos_inspeccion_restaurar();
            if(count($datos_inspeccion) > 0){
                // Asignando a las variables de sesion
                $session->set('inspeccion', $datos_inspeccion[0]['No_Inspeccion']);
                $session->set('nombreSitio', $datos_inspeccion[0]['nombreSitio']);
                $session->set('Id_Sitio', $datos_inspeccion[0]['Id_Sitio']);
                $session->set('Id_Inspeccion', $datos_inspeccion[0]['Id_Inspeccion']);
                $session->set('Id_Status_Inspeccion', $datos_inspeccion[0]['Id_Status_Inspeccion']);
            }
        }

        // Cargando vista y sus datos
        $dataMenu = datos_menu($session);
        $script = ['src'  => 'js/inventarios.js'];

        echo view("templetes/header");
        echo view("dashboard/modulos/menu",$dataMenu);
        echo view("dashboard/modulos/inventarios");
        echo view('templetes/footer',$script);
    }

    /* LISTADO ESTATUS INSPECCION DETALLE PARA CREAR SELECT*/
    public function obtenerEstatusInspecDet(){
        $estatusInspecDetMdl = new EstatusInspecDetMdl();
        echo (json_encode($estatusInspecDetMdl->obtenerLista()));
    }

    /* OBTENER ESTRUCTURA DEL ARBOL */
    Public function obtenerNodosArbol(){
        $inventariosMdl = new InventariosMdl();

        $data = $inventariosMdl->obtenerNodosArbol(
            $this->request->getPost('Id_Sitio'),
            $this->request->getPost('Id_Inspeccion'),
            $this->request->getPost('parentId')
        );
        
        echo (json_encode($data));
    }

    /* CREAR GUARDAR UBICACIONES */
    public function nuevo(){
        $ubicacionesMdl = new UbicacionesMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        (!empty($this->request->getPost('Es_Equipo'))) ? $es_equipo = 'SI' : $es_equipo = 'NO';

        if ($this->request->getPost('Nivel_arbol') > 1) {
            $ruta = $this->request->getPost('ruta_nueva_ubicacion')." / ".$this->request->getPost('Ubicacion');
        }else{
            $ruta = $this->request->getPost('Ubicacion');
        }

        // CREAMOS EL ID CON LA AYUDA DEL HELPER Y LO GUARDAMOS EN LA VARIABLE $Id_Ubicacion_insert
        // PARA PASARLO AL INSERT Y DESPUES USARLO EN LA VALIDACION DE EXITO DE LA INSERCION
        $Id_Ubicacion_insert = crear_id();

        $saveUbicacion = $ubicacionesMdl->insert([
            'Id_Ubicacion'      =>$Id_Ubicacion_insert,
            'Id_Sitio'          =>$this->request->getPost('Id_Sitio'),
            'Id_Ubicacion_padre'=>$this->request->getPost('Id_Ubicacion_padre'),
            'Es_Equipo'         =>$es_equipo,
            'Nivel_arbol'       =>$this->request->getPost('Nivel_arbol'),
            'Id_Sitio'          =>$this->request->getPost('Id_Sitio'),
            'Ubicacion'         =>$this->request->getPost('Ubicacion'),
            'Descripcion'       =>$this->request->getPost('Descripcion'),
            'Id_Tipo_Prioridad' =>$this->request->getPost('Id_Tipo_Prioridad'),
            'Codigo_Barras'     =>$this->request->getPost('Codigo_Barras'),
            'Fabricante'        =>$this->request->getPost('Id_Fabricante'),
            'Ruta'              =>$ruta,
            'Estatus'           =>'Activo',
            'Creado_Por'        =>$session->Id_Usuario,
            'Fecha_Creacion'    =>date("Y-m-d H:i:s"),
        ]);

        // HACEMOS UNA CONSULTA CON EL ID GENERADO,SI SE ENCUENTRA EN LA TABLA RETORNA LOS DATOS Y 
        // PASA POR LA VALIDACION DE SI ES NULL, SE NIEGA EL RESULTADO
        // SI EXISTEN DATOS EN LA BD QUIERE DECIR QUE SE HIZO EL ALTA ASI QUE NO ES NULL Y SE NIEGA CONVIRTIENOSE EN TRUE
        // Y SI ES NULL SE NIEGA Y SE CONVIERTE A FALSE
        $save = !is_null($ubicacionesMdl->get($Id_Ubicacion_insert));

        // El tipo de inspeccion de donde se saca??
        // 'Id_Tipo_Inspeccion'    =>$this->request->getPost('Id_Tipo_Inspeccion'),

        // Para que entre al succes del ajax        
        if($save != false){

            // CREAMOS EL ID CON LA AYUDA DEL HELPER Y LO GUARDAMOS EN LA VARIABLE $Id_Inspeccion_Det_insert
            // PARA PASARLO AL INSERT Y DESPUES USARLO EN LA VALIDACION DE EXITO DE LA INSERCION
            $Id_Inspeccion_Det_insert = crear_id();

            $saveDetalle = $inspeccionesDetMdl->insert([
                'Id_Inspeccion_Det'       =>$Id_Inspeccion_Det_insert,
                'Id_Inspeccion'           =>$this->request->getPost('Id_Inspeccion'),
                'Id_Ubicacion'            =>$Id_Ubicacion_insert,
                'Id_Status_Inspeccion_Det'=>$this->request->getPost('Test_Estatus'),
                // 'Notas_Inspeccion'        =>'',
                'Estatus'                 =>'Activo',
                'Creado_Por'              =>$session->Id_Usuario,
                'Fecha_Creacion'          =>date("Y-m-d H:i:s"),
            ]);

            // HACEMOS UNA CONSULTA CON EL ID GENERADO,SI SE ENCUENTRA EN LA TABLA RETORNA LOS DATOS Y 
            // PASA POR LA VALIDACION DE SI ES NULL, SE NIEGA EL RESULTADO
            // SI EXISTEN DATOS EN LA BD QUIERE DECIR QUE SE HIZO EL ALTA ASI QUE NO ES NULL Y SE NIEGA CONVIRTIENOSE EN TRUE
            // Y SI ES NULL SE NIEGA Y SE CONVIERTE A FALSE
            $save_inspection_det = !is_null($inspeccionesDetMdl->get($Id_Inspeccion_Det_insert));

            // Para que entre al succes del ajax
            if($save_inspection_det != false){

                $this->cambiarEstatusUbicacion($Id_Inspeccion_Det_insert, $this->request->getPost('Test_Estatus'));

                echo json_encode(array("status" => true, "Id_Inspeccion_Det" => $Id_Inspeccion_Det_insert));
            }else{
                echo json_encode(array("status" => false));
            }

        }else{
            echo json_encode(array("status" => false ));
        }
    }

    /* UPDATE UBICACIONES */
    public function update(){
        $ubicacionesMdl = new UbicacionesMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        (!empty($this->request->getPost('Es_Equipo'))) ? $es_equipo = 'SI' : $es_equipo = 'NO';

        $updateUbicacion = $ubicacionesMdl->update(
            $this->request->getPost('Id_Ubicacion'),[
            'Id_Ubicacion_padre'=>$this->request->getPost('Id_Ubicacion_padre'),
            'Es_Equipo'         =>$es_equipo,
            'Nivel_arbol'       =>$this->request->getPost('Nivel_arbol'),
            'Ubicacion'         =>$this->request->getPost('Ubicacion'),
            'Descripcion'       =>$this->request->getPost('Descripcion'),
            'Id_Tipo_Prioridad' =>$this->request->getPost('Id_Tipo_Prioridad'),
            'Codigo_Barras'     =>$this->request->getPost('Codigo_Barras'),
            'Fabricante'        =>$this->request->getPost('Id_Fabricante'),
            'Estatus'           =>'Activo',
            'Modificado_Por'    =>$session->Id_Usuario,
            'Fecha_Mod'         =>date("Y-m-d H:i:s"),
        ]);

        $updateDetalle = $inspeccionesDetMdl->update([
            'Id_Inspeccion_Det' => $this->request->getPost('Id_Inspeccion_Det'),
            'Id_Inspeccion' => $this->request->getPost('Id_Inspeccion')
        ],[
            'Id_Status_Inspeccion_Det'=>$this->request->getPost('Test_Estatus'),
            // 'Notas_Inspeccion'        =>'',
            'Estatus'                 =>'Activo',
            'Modificado_Por'          =>$session->Id_Usuario,
            'Fecha_Mod'               =>date("Y-m-d H:i:s")
        ]);

        // El tipo de inspeccion de donde se saca??
        // 'Id_Tipo_Inspeccion'    =>$this->request->getPost('Id_Tipo_Inspeccion'),
        // Para que entre al succes del ajax
        // if($updateUbicacion != false && $updateDetalle != false){
        if($updateUbicacion != false){

            $this->cambiarEstatusUbicacion($this->request->getPost('Id_Inspeccion_Det'), $this->request->getPost('Test_Estatus'));

            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    /* BORRAR UBICACIONES */
    public function borrar($id){
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        // Primero actualizamos los estatus con sus colores porque cuando se elimine ya no encuentra los elementos
        $this->cambiarEstatusUbicacion($id, "568798D2-76BB-11D3-82BF-00104BC75DC2");

        $delete = $inspeccionesDetMdl->update(
            $id,[
            'Estatus'       => 'Inactivo',
            'Modificado_Por'=>$session->Id_Usuario,
            'Fecha_Mod'     => date("Y-m-d H:i:s")
        ]);

        if($delete){
            // echo ('{"success":true,"msg":"Registro eliminado","tree":'.json_encode($this->obtener()).'}');
            echo json_encode(array("status" => true ));
        }
        else{
            return json_encode(500);
        }
    }

    public function setSelectedNode(){
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();
        $db = db_connect();

        $db->query("UPDATE inspecciones_det SET selected = '0' WHERE Id_Inspeccion = '".$session->Id_Inspeccion."'");
        
        $update = $inspeccionesDetMdl->update(
            $this->request->getPost('Id_Inspeccion_Det'),[
            'selected'          => 1,
            'Modificado_Por'    =>$session->Id_Usuario,
            'Fecha_Mod'         =>date("Y-m-d H:i:s")
        ]);

        return;
    }

    public function setUnselectedNode(){
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();
        $db = db_connect();

        $db->query("UPDATE inspecciones_det SET selected = '0' Id_Inspeccion = '".$session->Id_Inspeccion."'");
        
        return;
    }

    public function setExpandNode(){
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        $update = $inspeccionesDetMdl->update(
            $this->request->getPost('Id_Inspeccion_Det'),[
            'expanded'          => $this->request->getPost('expanded'),
            'Modificado_Por'    =>$session->Id_Usuario,
            'Fecha_Mod'         =>date("Y-m-d H:i:s")
        ]);

        if($update){
            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    public function validar_eliminacion_ubicacion($id_inspec_det, $id_ubicacion){
        $inventariosMdl = new InventariosMdl();
        $problemasMdl = new ProblemasMdl();
        $lineaBaseMdl = new LineaBaseMdl();
        $session = session();

        // Validando que la ubicacion a eliminar no tenga sub elementos o problemas o BL
        $sub_ubicaciones = $inventariosMdl->where(["parent_id" => $id_ubicacion])->findAll();
        $problemas = $problemasMdl->where(["Id_Inspeccion_Det" => $id_inspec_det, "Id_Inspeccion" => $session->Id_Inspeccion])->findAll();
        $baseLine = $lineaBaseMdl->where(["Id_Inspeccion_Det" => $id_inspec_det, "Id_Inspeccion" => $session->Id_Inspeccion])->findAll();

        $msj = "Esta ubicacíon contiene:<br>";

        if(count($sub_ubicaciones) > 0){
            $msj .= "Subelementos<br>";
        }
        if(count($problemas) > 0){
            $msj .= "Hallazgos<br>";
        }
        if(count($baseLine) > 0){
            $msj .= "Baseline";
        }

        if(count($sub_ubicaciones) > 0 || count($problemas) > 0 || count($baseLine) > 0){
            return json_encode(array("status" => false, "msj" => $msj));            
        }else{
            return json_encode(array("status" => true, "msj" => "" ));
        }
    }

    public function validar_crear_baseline($id){
        $ubicacionesMdl = new UbicacionesMdl();
        $problemasMdl = new ProblemasMdl();
        $lineaBaseMdl = new LineaBaseMdl();
        $session = session();

        // Validando que la ubicacion a eliminar no tenga sub elementos o problemas o BL
        $problemas = $problemasMdl->where(["Id_Inspeccion_Det" => $id, "Id_Inspeccion" => $session->Id_Inspeccion, "Estatus" => "Activo"])->findAll();
        $baseLine = $lineaBaseMdl->where(["Id_Inspeccion_Det" => $id, "Id_Inspeccion" => $session->Id_Inspeccion, "Estatus" => "Activo"])->findAll();

        $msj = "Esta ubicacíon contiene:<br>";

        if(count($problemas) > 0){
            $msj .= "Hallazgos<br>";
        }
        if(count($baseLine) > 0){
            $msj .= "Baseline";
        }

        if(count($problemas) > 0 || count($baseLine) > 0){
            return json_encode(array("status" => false, "msj" => $msj));            
        }else{
            return json_encode(array("status" => true, "msj" => "" ));
        }
    }

    public function validar_crear_problema_ubicacion($id){
        $lineaBaseMdl = new LineaBaseMdl();
        $session = session();

        $baseLine = $lineaBaseMdl->where(["Id_Inspeccion_Det" => $id, "Id_Inspeccion" => $session->Id_Inspeccion])->findAll();

        if(count($baseLine) > 0){
            return json_encode(array("status" => false, "msj" => "Este equipo contiene Baseline"));            
        }else{
            return json_encode(array("status" => true, "msj" => "" ));
        }
    }

    public function getNumero_Problema(){
        $problemasMdl = new ProblemasMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();

        $Id_Inspeccion = $this->request->getPost('Id_Inspeccion');
        $Id_Tipo_Inspeccion = $this->request->getPost('Id_Tipo_Inspeccion');
        $id_ubicacion_original = $this->request->getPost('id_ubicacion_original');

        if(is_null($id_ubicacion_original)){
            echo(json_encode($problemasMdl->getNumero_Problema($Id_Inspeccion,$Id_Tipo_Inspeccion)));
        }else{
            $numero_problema_actual = $problemasMdl->getNumero_Problema($Id_Inspeccion,$Id_Tipo_Inspeccion);
            $id_inspeccion_detalle_actual = $inspeccionesDetMdl->obtener_idInspeccionDet_actual($Id_Inspeccion,$id_ubicacion_original);
            echo json_encode(array("numero_problema_actual" => $numero_problema_actual, "id_inspeccion_detalle_actual" => $id_inspeccion_detalle_actual));            
        }
    }

    public function getProblemas_Sitio($id){
        $problemasMdl = new ProblemasMdl();
        
        $condicion = array("problemas.Id_Sitio" => $id);
        $orden = 'problemas.Fecha_Creacion ASC';
        echo (json_encode($problemasMdl->getProblemas_Sitio($condicion,$orden)));
    }

    /* GUARDAR PROBLEMAS */
    public function nuevoProblema(){
        $problemasMdl = new ProblemasMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        // CREAMOS EL ID CON LA AYUDA DEL HELPER Y LO GUARDAMOS EN LA VARIABLE $Id_Problema_insert
        // PARA PASARLO AL INSERT Y DESPUES USARLO EN LA VALIDACION DE EXITO DE LA INSERCION
        $Id_Problema_insert = crear_id();

        $tipoInspeccion= $this->request->getPost('Id_Tipo_Inspeccion');
        $numInspeccion = $this->request->getPost('strNumInspeccion');
        $idInspeccion = $this->request->getPost('Id_Inspeccion');

        $data = [
            'Id_Problema'          =>$Id_Problema_insert,
            'Id_Tipo_Inspeccion'   =>$tipoInspeccion,
            'Numero_Problema'      =>$this->request->getPost('Numero_Problema'),
            'Id_Sitio'             =>$this->request->getPost('Id_Sitio'),
            'Id_Inspeccion'        =>$idInspeccion,
            'Id_Inspeccion_Det'    =>$this->request->getPost('Id_Inspeccion_Det'),
            'Id_Ubicacion'         =>$this->request->getPost('Id_Ubicacion'),
            // 'Problem_Temperature'  =>$Problem_Temp,
            // 'Reference_Temperature'=>$Reference_Temp,
            'Problem_Phase'        =>$this->request->getPost('Problem_Phase'),
            'Reference_Phase'      =>$this->request->getPost('Reference_Phase'),
            'Problem_Rms'          =>$this->request->getPost('Problem_Rms'),
            'Reference_Rms'        =>$this->request->getPost('Reference_Rms'),
            'Additional_Info'      =>$this->request->getPost('Additional_Info'),
            'Additional_Rms'       =>$this->request->getPost('Additional_Rms'),
            'Emissivity_Check'     =>$this->request->getPost('Emissivity_Check') === 'on' ? 'on' : 'off',
            'Emissivity'           =>$this->request->getPost('Emissivity'),
            'Indirect_Temp_Check'  =>$this->request->getPost('Indirect_Temp_Check') === 'on' ? 'on' : 'off',
            'Temp_Ambient_Check'   =>$this->request->getPost('Temp_Ambient_Check') === 'on' ? 'on' : 'off',
            'Temp_Ambient'         =>$this->request->getPost('Temp_Ambient'),
            'Environment_Check'    =>$this->request->getPost('Environment_Check') === 'on' ? 'on' : 'off',
            'Environment'          =>$this->request->getPost('Environment'),
            // 'Ir_File'              =>$nombreIrImagen,
            // 'Photo_File'           =>$nombreDigImagen,
            'Wind_Speed_Check'     =>$this->request->getPost('Wind_Speed_Check') === 'on' ? 'on' : 'off',
            'Wind_Speed'           =>$this->request->getPost('Wind_Speed'),
            'Id_Fabricante'        =>$this->request->getPost('Id_Fabricante'),
            'Rated_Load_Check'     =>$this->request->getPost('Rated_Load_Check') === 'on' ? 'on' : 'off',
            'Rated_Load'           =>$this->request->getPost('Rated_Load'),
            'Circuit_Voltage_Check'=>$this->request->getPost('Circuit_Voltage_Check') === 'on' ? 'on' : 'off',
            'Circuit_Voltage'      =>$this->request->getPost('Circuit_Voltage'),
            'Id_Falla'             =>$this->request->getPost('Id_Falla'),            
            // 'Component_Comment'    =>$this->request->getPost('Component_Comment'),
            'Estatus_Problema'     =>"Abierto",
            // 'Aumento_Temperatura'  =>$diferencia_Temp,
            // 'Id_Severidad'         =>$this->severidad($diferencia_Temp),
            'Estatus'              =>"Activo",
            'Es_Cronico'           =>"NO",
            'Rpm'                  =>$this->request->getPost('Rpm'),
            'Bearing_Type'         =>$this->request->getPost('Bearing_Type'),
            // 'Ruta'                 =>$this->request->getPost('StrRuta'),
            'Creado_Por'           =>$session->Id_Usuario,
            'Fecha_Creacion'       =>date("Y-m-d H:i:s"),
        ];

        if($tipoInspeccion == "0D32B331-76C3-11D3-82BF-00104BC75DC2" || $tipoInspeccion == "0D32B332-76C3-11D3-82BF-00104BC75DC2" || $tipoInspeccion == "0D32B334-76C3-11D3-82BF-00104BC75DC2") {

            $Problem_Temp =$this->request->getPost('Problem_Temperature');
            $Reference_Temp =$this->request->getPost('Reference_Temperature');
            $diferencia_Temp = ($Problem_Temp - $Reference_Temp);
            
            $data['Id_Causa_Raiz']         =$this->request->getPost('Id_Causa_Raiz');
            $data['Problem_Temperature']   =$Problem_Temp;
            $data['Reference_Temperature'] =$Reference_Temp;
            $data['Component_Comment']     =$this->request->getPost('Component_Comment');
            $data['Aumento_Temperatura']   =$diferencia_Temp;
            $data['Id_Severidad']          =$this->severidad($diferencia_Temp);
            $data['Ruta']                  =$this->request->getPost('StrRuta');

        }else{

            // $data['hazard_Type']          =$this->request->getPost('hazard_Type');
            // $data['hazard_Classification']=$this->request->getPost('hazard_Classification');
            // $data['hazard_Group']         =$this->request->getPost('hazard_Group');
            $data['Id_Causa_Raiz']         =$this->request->getPost('Id_Causa_Raiz_Visual');
            $data['Id_Recomendacion']     =$this->request->getPost('Id_Recomendacion');
            $data['hazard_Issue']         =$this->request->getPost('hazard_Issue');
            $data['Component_Comment']    =$this->request->getPost('observaciones_Visual');
            $data['Id_Severidad']         =$this->request->getPost('Id_Severidad');
            $data['Ruta']                 =$this->request->getPost('StrRutaVisual');
        }

        // Obteniendo la imagen IR
        $nombreIrImagen = $this->request->getPost('Ir_File');
        // Obteniendo la foto digital
        $nombreDigImagen = $this->request->getPost('Photo_File');

        if ($nombreIrImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreIrImagen,1);}
        if ($nombreDigImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreDigImagen,2);}

        $data['Ir_File'] = $nombreIrImagen;
        $data['Photo_File'] = $nombreDigImagen;
        // Realizando el insert de la data
        $saveProblema = $problemasMdl->insert($data);

        // HACEMOS UNA CONSULTA CON EL ID GENERADO,SI SE ENCUENTRA EN LA TABLA RETORNA LOS DATOS Y 
        // PASA POR LA VALIDACION DE SI ES NULL, SE NIEGA EL RESULTADO
        // SI EXISTEN DATOS EN LA BD QUIERE DECIR QUE SE HIZO EL ALTA ASI QUE NO ES NULL Y SE NIEGA CONVIRTIENOSE EN TRUE
        // Y SI ES NULL SE NIEGA Y SE CONVIERTE A FALSE
        $saveProblema = !is_null($problemasMdl->get($Id_Problema_insert));

        $updateDetalle = $inspeccionesDetMdl->update([
            'Id_Inspeccion_Det' => $this->request->getPost('Id_Inspeccion_Det'),
            'Id_Inspeccion' => $this->request->getPost('Id_Inspeccion'),
        ],[
            'Id_Status_Inspeccion_Det'=>"568798D2-76BB-11D3-82BF-00104BC75DC2",
            'Id_Estatus_Color_Text'   =>"2",
            'Modificado_Por'          =>$session->Id_Usuario,
            'Fecha_Mod'               =>date("Y-m-d H:i:s"),
        ]);

        if($saveProblema != false){

            $this->actualizarEstatusElementoPadre($this->request->getPost('Id_Inspeccion_Det'));
            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }

    }

    public function updateProblema(){
        $problemasMdl = new ProblemasMdl();
        $session = session();

        $tipoInspeccion= $this->request->getPost('Id_Tipo_Inspeccion');
        // $idInspeccion = $this->request->getPost('Id_Inspeccion');
        $idInspeccion = $session->Id_Inspeccion;

        $data = [
            'Id_Tipo_Inspeccion'   =>$this->request->getPost('Id_Tipo_Inspeccion'),
            // 'Id_Sitio'             =>$this->request->getPost('Id_Sitio'),
            // 'Id_Inspeccion'        =>$idInspeccion,
            // 'Id_Inspeccion_Det'    =>$this->request->getPost('Id_Inspeccion_Det'),
            // 'Id_Ubicacion'         =>$this->request->getPost('Id_Ubicacion'),
            'Problem_Phase'        =>$this->request->getPost('Problem_Phase'),
            'Reference_Phase'      =>$this->request->getPost('Reference_Phase'),
            'Problem_Rms'          =>$this->request->getPost('Problem_Rms'),
            'Reference_Rms'        =>$this->request->getPost('Reference_Rms'),
            'Additional_Info'      =>$this->request->getPost('Additional_Info'),
            'Additional_Rms'       =>$this->request->getPost('Additional_Rms'),
            'Emissivity_Check'     =>$this->request->getPost('Emissivity_Check') === 'on' ? 'on' : 'off',
            'Emissivity'           =>$this->request->getPost('Emissivity'),
            'Indirect_Temp_Check'  =>$this->request->getPost('Indirect_Temp_Check') === 'on' ? 'on' : 'off',
            'Temp_Ambient_Check'   =>$this->request->getPost('Temp_Ambient_Check') === 'on' ? 'on' : 'off',
            'Temp_Ambient'         =>$this->request->getPost('Temp_Ambient'),
            'Environment_Check'    =>$this->request->getPost('Environment_Check') === 'on' ? 'on' : 'off',
            'Environment'          =>$this->request->getPost('Environment'),
            'Wind_Speed_Check'     =>$this->request->getPost('Wind_Speed_Check') === 'on' ? 'on' : 'off',
            'Wind_Speed'           =>$this->request->getPost('Wind_Speed'),
            'Id_Fabricante'        =>$this->request->getPost('Id_Fabricante'),
            'Rated_Load_Check'     =>$this->request->getPost('Rated_Load_Check') === 'on' ? 'on' : 'off',
            'Rated_Load'           =>$this->request->getPost('Rated_Load'),
            'Circuit_Voltage_Check'=>$this->request->getPost('Circuit_Voltage_Check') === 'on' ? 'on' : 'off',
            'Circuit_Voltage'      =>$this->request->getPost('Circuit_Voltage'),
            'Id_Falla'             =>$this->request->getPost('Id_Falla'),
            'Estatus_Problema'     =>$this->request->getPost('Estatus_Problema') === 'on' ? 'Cerrado' : 'Abierto',
            'Cerrado_En_Inspeccion'=>$this->request->getPost('Estatus_Problema') === 'on' ? $idInspeccion : 0,
            'Estatus'              =>"Activo",
            'Rpm'                  =>$this->request->getPost('Rpm'),
            'Bearing_Type'         =>$this->request->getPost('Bearing_Type'),
            'Modificado_Por'       =>$session->Id_Usuario,
            'Fecha_Mod'            =>date("Y-m-d H:i:s"),
        ];

        if($tipoInspeccion == "0D32B331-76C3-11D3-82BF-00104BC75DC2" || $tipoInspeccion == "0D32B332-76C3-11D3-82BF-00104BC75DC2" || $tipoInspeccion == "0D32B334-76C3-11D3-82BF-00104BC75DC2") {

            $numInspeccion =$this->request->getPost('strNumInspeccionEdit');

            $Problem_Temp =$this->request->getPost('Problem_Temperature');
            $Reference_Temp =$this->request->getPost('Reference_Temperature');
            $diferencia_Temp = ($Problem_Temp - $Reference_Temp);

            $data['Numero_Problema']       =$this->request->getPost('Numero_ProblemaEdit');
            $data['Problem_Temperature']   =$Problem_Temp;
            $data['Reference_Temperature'] =$Reference_Temp;
            $data['Component_Comment']     =$this->request->getPost('Component_Comment');
            $data['Aumento_Temperatura']   =$diferencia_Temp;
            $data['Id_Severidad']          =$this->severidad($diferencia_Temp);
            $data['Ruta']                  =$this->request->getPost('StrRuta');

        }else{

            $numInspeccion =$this->request->getPost('strNumInspeccionEditVisual');

            $data['Numero_Problema']      =$this->request->getPost('Numero_ProblemaEditVisual');
            // $data['hazard_Type']          =$this->request->getPost('hazard_Type');
            // $data['hazard_Classification']=$this->request->getPost('hazard_Classification');
            // $data['hazard_Group']         =$this->request->getPost('hazard_Group');
            $data['hazard_Issue']         =$this->request->getPost('hazard_Issue');
            $data['Component_Comment']    =$this->request->getPost('observaciones_Visual');
            $data['Id_Severidad']         =$this->request->getPost('Id_Severidad');
            $data['Ruta']                 =$this->request->getPost('StrRutaVisual');
        }

        // Obteniendo la imagen IR
        $nombreIrImagen = $this->request->getPost('Ir_File');
        // Obteniendo la foto digital
        $nombreDigImagen = $this->request->getPost('Photo_File');

        if ($nombreIrImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreIrImagen,1);}
        if ($nombreDigImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreDigImagen,2);}

        $data['Ir_File'] = $nombreIrImagen;
        $data['Photo_File'] = $nombreDigImagen;

        $updateProblema = $problemasMdl->update($this->request->getPost('Id_Problema'),$data);

        if($updateProblema != false){
            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    public function guardarCronico(){
        $historialProblemas = new HistorialProblemasMdl();
        $problemasMdl = new ProblemasMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        // CREAMOS EL ID CON LA AYUDA DEL HELPER Y LO GUARDAMOS EN LA VARIABLE $Id_Problema_insert
        // PARA PASARLO AL INSERT Y DESPUES USARLO EN LA VALIDACION DE EXITO DE LA INSERCION
        $Id_Problema_insert = crear_id();

        $tipoInspeccion= $this->request->getPost('Id_Tipo_Inspeccion');
        $idInspeccion = $this->request->getPost('Id_Inspeccion');

        // Creamos un nuevo problema cronico
        $data = [
            'Id_Problema'          =>$Id_Problema_insert,
            'Id_Tipo_Inspeccion'   =>$tipoInspeccion,
            'Id_Sitio'             =>$this->request->getPost('Id_Sitio'),
            'Id_Inspeccion'        =>$idInspeccion,
            'Id_Inspeccion_Det'    =>$this->request->getPost('Id_Inspeccion_Det_Cronico'),
            'Id_Ubicacion'         =>$this->request->getPost('Id_Ubicacion'),
            // 'Problem_Temperature'  =>$Problem_Temp,
            // 'Reference_Temperature'=>$Reference_Temp,
            'Problem_Phase'        =>$this->request->getPost('Problem_Phase'),
            'Reference_Phase'      =>$this->request->getPost('Reference_Phase'),
            'Problem_Rms'          =>$this->request->getPost('Problem_Rms'),
            'Reference_Rms'        =>$this->request->getPost('Reference_Rms'),
            'Additional_Info'      =>$this->request->getPost('Additional_Info'),
            'Additional_Rms'       =>$this->request->getPost('Additional_Rms'),
            'Emissivity_Check'     =>$this->request->getPost('Emissivity_Check') === 'on' ? 'on' : 'off',
            'Emissivity'           =>$this->request->getPost('Emissivity'),
            'Indirect_Temp_Check'  =>$this->request->getPost('Indirect_Temp_Check') === 'on' ? 'on' : 'off',
            'Temp_Ambient_Check'   =>$this->request->getPost('Temp_Ambient_Check') === 'on' ? 'on' : 'off',
            'Temp_Ambient'         =>$this->request->getPost('Temp_Ambient'),
            'Environment_Check'    =>$this->request->getPost('Environment_Check') === 'on' ? 'on' : 'off',
            'Environment'          =>$this->request->getPost('Environment'),
            // 'Ir_File'              =>$nombreIrImagen,
            // 'Photo_File'           =>$nombreDigImagen,
            'Wind_Speed_Check'     =>$this->request->getPost('Wind_Speed_Check') === 'on' ? 'on' : 'off',
            'Wind_Speed'           =>$this->request->getPost('Wind_Speed'),
            'Id_Fabricante'        =>$this->request->getPost('Id_Fabricante'),
            'Rated_Load_Check'     =>$this->request->getPost('Rated_Load_Check') === 'on' ? 'on' : 'off',
            'Rated_Load'           =>$this->request->getPost('Rated_Load'),
            'Circuit_Voltage_Check'=>$this->request->getPost('Circuit_Voltage_Check') === 'on' ? 'on' : 'off',
            'Circuit_Voltage'      =>$this->request->getPost('Circuit_Voltage'),
            'Id_Falla'             =>$this->request->getPost('Id_Falla'),
            // 'Component_Comment'    =>$this->request->getPost('Component_Comment'),
            'Estatus_Problema'     =>"Abierto",
            // 'Aumento_Temperatura'  =>$diferencia_Temp,
            // 'Id_Severidad'         =>$this->severidad($diferencia_Temp),
            'Estatus'              =>"Activo",
            'Es_Cronico'           =>"SI",
            'Rpm'                  =>$this->request->getPost('Rpm'),
            'Bearing_Type'         =>$this->request->getPost('Bearing_Type'),
            // 'Ruta'                 =>$this->request->getPost('StrRuta'),
            'Creado_Por'           =>$session->Id_Usuario,
            'Fecha_Creacion'       =>date("Y-m-d H:i:s"),
        ];

        if($tipoInspeccion == "0D32B331-76C3-11D3-82BF-00104BC75DC2" || $tipoInspeccion == "0D32B332-76C3-11D3-82BF-00104BC75DC2" || $tipoInspeccion == "0D32B334-76C3-11D3-82BF-00104BC75DC2") {

            $numInspeccion = $this->request->getPost('strNumInspeccionEdit');

            $Problem_Temp =$this->request->getPost('Problem_Temperature');
            $Reference_Temp =$this->request->getPost('Reference_Temperature');
            $diferencia_Temp = ($Problem_Temp - $Reference_Temp);

            $data['Id_Causa_Raiz']         =$this->request->getPost('Id_Causa_Raiz');
            $data['Numero_Problema']       =$this->request->getPost('Numero_ProblemaEdit');
            $data['Problem_Temperature']   =$Problem_Temp;
            $data['Reference_Temperature'] =$Reference_Temp;
            $data['Component_Comment']     =$this->request->getPost('Component_Comment');
            $data['Aumento_Temperatura']   =$diferencia_Temp;
            $data['Id_Severidad']          =$this->severidad($diferencia_Temp);
            $data['Ruta']                  =$this->request->getPost('StrRuta');

        }else{

            $numInspeccion = $this->request->getPost('strNumInspeccionEditVisual');

            $data['Id_Causa_Raiz']         =$this->request->getPost('Id_Causa_Raiz_Visual');
            $data['Numero_Problema']       =$this->request->getPost('Numero_ProblemaEditVisual');
            $data['hazard_Type']          =$this->request->getPost('hazard_Type');
            $data['hazard_Classification']=$this->request->getPost('hazard_Classification');
            $data['hazard_Group']         =$this->request->getPost('hazard_Group');
            $data['hazard_Issue']         =$this->request->getPost('hazard_Issue');
            $data['Component_Comment']    =$this->request->getPost('observaciones_Visual');
            $data['Id_Severidad']         =$this->request->getPost('Id_Severidad');
            $data['Ruta']                 =$this->request->getPost('StrRutaVisual');
        }

        // Obteniendo la imagen IR
        $nombreIrImagen = $this->request->getPost('Ir_File');
        // Obteniendo la foto digital
        $nombreDigImagen = $this->request->getPost('Photo_File');

        if ($nombreIrImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreIrImagen,1);}
        if ($nombreDigImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreDigImagen,2);}

        $data['Ir_File'] = $nombreIrImagen;
        $data['Photo_File'] = $nombreDigImagen;

        // Realizando el insert de la data
        $saveProblema = $problemasMdl->insert($data);


        // HACEMOS UNA CONSULTA CON EL ID GENERADO,SI SE ENCUENTRA EN LA TABLA RETORNA LOS DATOS Y 
        // PASA POR LA VALIDACION DE SI ES NULL, SE NIEGA EL RESULTADO
        // SI EXISTEN DATOS EN LA BD QUIERE DECIR QUE SE HIZO EL ALTA ASI QUE NO ES NULL Y SE NIEGA CONVIRTIENOSE EN TRUE
        // Y SI ES NULL SE NIEGA Y SE CONVIERTE A FALSE
        $saveProblema = !is_null($problemasMdl->get($Id_Problema_insert));


        // Obteniendo el id ddel nuevo problea que ahora es cronico
        $ultimoIdProblema = $Id_Problema_insert;
        // Obteniendo el id del problmea del que se hizo el uevo problmea cronico
        $idProblemaAnterior = $this->request->getPost('Id_Problema');
        // Obteniendo el problmea original
        $idProblemaOriginal = $historialProblemas->getproblemaOrigian($idProblemaAnterior);
        $idProblemaOriginal = count($idProblemaOriginal) < 1 ? $idProblemaAnterior : $idProblemaOriginal[0]["Id_Problema_Original"];
        
        // CREAMOS EL ID CON LA AYUDA DEL HELPER Y LO GUARDAMOS EN LA VARIABLE $Id_Historial_Problema_insert
        $Id_Historial_Problema_insert = crear_id();
        
        // Creando el registro en el historial de problemas con todos los datos
        $saveHistorial = $historialProblemas->insert([
            'Id_Historial_Problema'=>$Id_Historial_Problema_insert,
            'Id_Problema'          =>$ultimoIdProblema,
            'Id_Problema_Anterior' =>$idProblemaAnterior,
            'Id_Problema_Original' =>$idProblemaOriginal,
            'Estatus'              =>"Activo",
            'Creado_Por'           =>$session->Id_Usuario,
            'Fecha_Creacion'       =>date("Y-m-d H:i:s"),
            'Id_Sitio'             =>$session->Id_Sitio
        ]);

        // Cerramos el problema original
        $updateProblema = $problemasMdl->update(
            $idProblemaAnterior,[
            'Estatus_Problema'     =>"Cerrado",
            'Es_Cronico'           =>"SI",
            'Modificado_Por'       =>$session->Id_Usuario,
            'Fecha_Mod'            =>date("Y-m-d H:i:s"),
        ]);

        $updateDetalle = $inspeccionesDetMdl->update(
            $this->request->getPost('Id_Inspeccion_Det_Cronico'),[
            'Id_Status_Inspeccion_Det'=>"568798D2-76BB-11D3-82BF-00104BC75DC2",
            'Id_Estatus_Color_Text'   =>"2",
            'Modificado_Por'          =>$session->Id_Usuario,
            'Fecha_Mod'               =>date("Y-m-d H:i:s")
        ]);

        if($saveProblema != false){
            $this->actualizarEstatusElementoPadre($this->request->getPost('Id_Inspeccion_Det_Cronico'));

            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    public function guardarBaseLine(){
        $lineaBaseMdl = new LineaBaseMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        $idBaseLine = $this->request->getPost('Id_Linea_Base');
        $idInspeccion = $this->request->getPost('Id_InspeccionBL');

        $nombreIrImagen = $this->request->getPost('Archivo_IR');
        $nombreDigImagen = $this->request->getPost('Archivo_ID');

        // CREAMOS EL ID CON LA AYUDA DEL HELPER Y LO GUARDAMOS EN LA VARIABLE $Id_Linea_Base_insert
        // PARA PASARLO AL INSERT Y DESPUES USARLO EN LA VALIDACION DE EXITO DE LA INSERCION
        $Id_Linea_Base_insert = crear_id();

        $data = [
            // 'Id_Linea_Base' =>$Id_Linea_Base_insert,
            // 'Id_Sitio'      =>$session->Id_Sitio,
            // 'Id_Ubicacion'  =>$this->request->getPost('Id_UbicacionBL'),
            // 'Id_Inspeccion' =>$idInspeccion,
            // 'Id_Inspeccion_Det'=> $this->request->getPost('Id_Inspeccion_Det_BL'),
            'MTA'           =>$this->request->getPost('MTA'),
            'Temp_max'      =>$this->request->getPost('Temp_max'),
            'Temp_amb'      =>$this->request->getPost('Temp_amb'),
            'Notas'         =>$this->request->getPost('NotasBL'),
            'Archivo_IR'    =>$nombreIrImagen,
            'Archivo_ID'    =>$nombreDigImagen,
            'Ruta'          =>$this->request->getPost('rutaBaseLine'),
            // 'Estatus'       =>"Activo",
            // 'Creado_Por'    =>$session->Id_Usuario,
            // 'Fecha_Creacion'=>date("Y-m-d H:i:s"),
        ];

        if ($nombreIrImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreIrImagen,1);}
        if ($nombreDigImagen != "") {$this->actualizarImgInicial($idInspeccion,$nombreDigImagen,2);}

        if($idBaseLine < 1){
            $data['Id_Linea_Base'] = $Id_Linea_Base_insert;
            $data['Id_Sitio'] = $session->Id_Sitio;
            $data['Id_Ubicacion'] = $this->request->getPost('Id_UbicacionBL');
            $data['Id_Inspeccion'] = $idInspeccion;
            $data['Id_Inspeccion_Det'] = $this->request->getPost('Id_Inspeccion_Det_BL');
            $data['Fecha_Creacion'] = date("Y-m-d H:i:s");
            $data['Estatus'] = "Activo";
            $data['Creado_Por'] = $session->Id_Usuario;
            $data['Fecha_Creacion'] = date("Y-m-d H:i:s");

            $saveBaseLine = $lineaBaseMdl->insert($data);
        }else{

            $data['Modificado_Por'] = $session->Id_Usuario;
            $data['Fecha_Mod'] = date("Y-m-d H:i:s");

            $saveBaseLine = $lineaBaseMdl->update($idBaseLine,$data);
        }

        // HACEMOS UNA CONSULTA CON EL ID GENERADO,SI SE ENCUENTRA EN LA TABLA RETORNA LOS DATOS Y 
        // PASA POR LA VALIDACION DE SI ES NULL, SE NIEGA EL RESULTADO
        // SI EXISTEN DATOS EN LA BD QUIERE DECIR QUE SE HIZO EL ALTA ASI QUE NO ES NULL Y SE NIEGA CONVIRTIENOSE EN TRUE
        // Y SI ES NULL SE NIEGA Y SE CONVIERTE A FALSE
        $saveBaseLine = !is_null($lineaBaseMdl->get($Id_Linea_Base_insert));

        if($saveBaseLine != false){

            if($idBaseLine < 1){
                // Si se guarda el nuevo BL, se actualiza el estatus de la copia de las ubicaciones a TESTED
                $updateDetalle = $inspeccionesDetMdl->update([
                    'Id_Inspeccion_Det_BL' => $this->request->getPost('Id_Inspeccion_Det_BL'),
                    'Id_InspeccionBL' => $this->request->getPost('Id_InspeccionBL'),
                ],[
                    'Id_Status_Inspeccion_Det'=>"568798D2-76BB-11D3-82BF-00104BC75DC2",
                    'Id_Estatus_Color_Text'   =>"3",
                    'Modificado_Por'          =>$session->Id_Usuario,
                    'Fecha_Mod'               =>date("Y-m-d H:i:s"),
                ]);
                
                $this->actualizarEstatusElementoPadre($this->request->getPost('Id_Inspeccion_Det_BL'));
            }

            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    public function eliminarBaseLine($id){
        $lineaBaseMdl = new LineaBaseMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();
        
        // Colocamos el color de texto a la ubicacionDetalleen negro
        $insp_det = $lineaBaseMdl->select('Id_Inspeccion_Det, Id_Inspeccion')->where(['Id_Linea_Base' => $id])->findAll();
        $inspeccionesDetMdl->update([
            'Id_Inspeccion_Det' => $insp_det[0]['Id_Inspeccion_Det'],
            'Id_Inspeccion' => $insp_det[0]['Id_Inspeccion'],
        ],[
            'Id_Status_Inspeccion_Det'=>"568798D1-76BB-11D3-82BF-00104BC75DC2",
            'Id_Estatus_Color_Text'   =>"1",
            'Modificado_Por'          =>$session->Id_Usuario,
            'Fecha_Mod'               =>date("Y-m-d H:i:s"),
        ]);

        $this->actualizarEstatusElementoPadre($insp_det[0]['Id_Inspeccion_Det']);

        // $delete = $lineaBaseMdl->update(
        //     $id,[
        //     'Estatus'       => 'Inactivo',
        //     'Modificado_Por'=>$session->Id_Usuario,
        //     'Fecha_Mod'     => date("Y-m-d H:i:s")
        // ]);

        $delete = $lineaBaseMdl->delete($id);

        if($delete){
            // echo ('{"success":true,"msg":"Registro eliminado","tree":'.json_encode($this->obtener()).'}');
            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    public function eliminarProblema($id){
        $problemasMdl = new ProblemasMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        $problema = $problemasMdl->select('Id_Tipo_Inspeccion, Id_Inspeccion, Id_Inspeccion_Det')->where(['Id_Problema' => $id])->findAll();
        // Todos los problemas de la npseccion por tipo de problema
        $todosLosProblemas = $problemasMdl->select('Id_Problema')
        ->where(['Id_Tipo_Inspeccion' => $problema[0]['Id_Tipo_Inspeccion'], 'Id_Inspeccion' => $problema[0]['Id_Inspeccion'],'Estatus' => 'Activo'])
        ->orderBy('Numero_Problema','ASC')
        ->findAll();

        $num = 1;
        foreach($todosLosProblemas as $problema_individual){
            $problemasMdl->update($problema_individual['Id_Problema'],['Numero_Problema' => $num]);
            $num = $num + 1;
        }

        // Haciendo la eliminacion logica
        $delete = $problemasMdl->update(
            $id,[
            'Estatus'       => 'Inactivo',
            'Modificado_Por'=>$session->Id_Usuario,
            'Fecha_Mod'     => date("Y-m-d H:i:s")
        ]);

        // Si ya ho hay mas problemas colocamos el color de texto a la ubicacionDetalleen negro
        $problemas_por_ubicacion = $problemasMdl->where(['Id_Inspeccion_Det' => $problema[0]['Id_Inspeccion_Det'],'Estatus' => 'Activo'])->findAll();
        if(count($problemas_por_ubicacion) < 1){
            $inspeccionesDetMdl->update([
                'Id_Inspeccion_Det' => $problema[0]['Id_Inspeccion_Det'],
                'Id_Inspeccion' => $problema[0]['Id_Inspeccion'],
            ],[
                'Id_Status_Inspeccion_Det'=>"568798D1-76BB-11D3-82BF-00104BC75DC2",
                'Id_Estatus_Color_Text'   =>"1",
                'Modificado_Por'          =>$session->Id_Usuario,
                'Fecha_Mod'               =>date("Y-m-d H:i:s"),
            ]);
        }

        $this->actualizarEstatusElementoPadre($problema[0]['Id_Inspeccion_Det']);

        // habilitar borrado hasta hacer trigger
        //$op = $problemasMdl->delete($id);

        if($delete){
            // echo ('{"success":true,"msg":"Registro eliminado","tree":'.json_encode($this->obtener()).'}');
            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    public function severidad($diferencia_Temp){
        if($diferencia_Temp < 1){
            $severidad = "1D56EDB4-8D6E-11D3-9270-006008A19766";
        }elseif($diferencia_Temp >= 1 && $diferencia_Temp <= 3){
            $severidad = "1D56EDB3-8D6E-11D3-9270-006008A19766";
        }elseif($diferencia_Temp >= 4 && $diferencia_Temp <= 8){
            $severidad = "1D56EDB2-8D6E-11D3-9270-006008A19766";
        }elseif($diferencia_Temp >= 9 && $diferencia_Temp <= 15){
            $severidad = "1D56EDB1-8D6E-11D3-9270-006008A19766";
        }else{
            $severidad = "1D56EDB0-8D6E-11D3-9270-006008A19766";
        }

        return $severidad;
    }

    public function borrarImagen($numInspeccion,$img){
        if($img != ""){
            unlink("Archivos_ETIC/inspecciones/".$numInspeccion."/Imagenes/".$img);
        }
        return;
    }

    public function getHistorialProblema($id_ubicacion, $Id_Tipo_Inspeccion){
        // $historialProblemas = new HistorialProblemasMdl();
        // echo (json_encode($historialProblemas->getHistorialProblema($id)));

        $problemasMdl = new ProblemasMdl();
        echo (json_encode($problemasMdl->getHistorialProblema($id_ubicacion, $Id_Tipo_Inspeccion)));
    }

    public function getHistorialBaseLine(){
        $lineaBaseMdl = new LineaBaseMdl();

        $Id_Ubicacion = $this->request->getPost('Id_Ubicacion');
        $Id_Inspeccion = $this->request->getPost('Id_Inspeccion');

        if ($Id_Ubicacion != '') {
            echo (json_encode($lineaBaseMdl->getBaselineUbicacion($Id_Ubicacion, $Id_Inspeccion)));
        }else {
            echo (json_encode($lineaBaseMdl->getHistorialBaseLine($Id_Inspeccion)));
        }

    }

    public function getHistorialInspecciones($id){
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        echo (json_encode($inspeccionesDetMdl->getHistorialInspecciones($id)));
    }

    public function lastImg(){
        $inspeccionesMdl = new InspeccionesMdl();

        $idInspeccion = $this->request->getPost('Id_Inspeccion');

        $imagenesIniciales = $inspeccionesMdl->select('IR_Imagen_Inicial, DIG_Imagen_Inicial')->where(['Id_Inspeccion' => $idInspeccion])->get();

        $data = [];
        $data["irImg"] = $imagenesIniciales[0]["IR_Imagen_Inicial"];
        $data["digImg"] = $imagenesIniciales[0]["DIG_Imagen_Inicial"];

        echo json_encode($data);
    }

    public function obtenerDatosImg($esReporte = 0, $rutaImgReporte = ""){
        $ruta = $this->request->getPost('ruta');
        $ruta = ROOTPATH."public/".$ruta;
        if ($esReporte == 1) {
            $ruta = $rutaImgReporte;
        }

        if(file_exists($ruta) && is_dir($ruta) == false){
            $fechaYhora = date("d/m/Y, g:i a", filemtime($ruta));

            $fechaYhora = explode(", ", $fechaYhora);
            $fecha = $fechaYhora[0];
            $hora = $fechaYhora[1];
        }else{
            $fecha = "--/--/----";
            $hora = "--:-- --";
        }

        if ($esReporte == 1) {
            return array("fecha" =>$fecha, "hora" => $hora);
        }else{
            echo json_encode(array("status" => true, "fecha" =>$fecha, "hora" => $hora));
        }

    }

    function subirImagenes() {
        $numInspeccion = $this->request->getPost('numInspeccionArchivos');
        $Id_Inspeccion = $this->request->getPost('Id_Inspeccion');


        if ($this->request->getFileMultiple('imagenes')) {
            foreach($this->request->getFileMultiple('imagenes') as $file){
                $nombreImg = $file->getClientName();
                $ruta = ROOTPATH.'public/Archivos_ETIC/inspecciones/'.$numInspeccion.'/Imagenes/';

                // Subiendo la imagen a la carpeta de la inspeccion
                $file->move($ruta, $nombreImg);

                // // Obteniendo la fecha y hora de la imagen
                // $ruta = $ruta.$nombreImg;
                // $fechaYhora = explode("-",date ("d/m/Y-g:i a", filemtime($ruta)));

                // // Creando el registro en la BD
                // $fotosProblemasMdl->insert([
                //     'Id_Inspeccion' =>$Id_Inspeccion,
                //     'Nombre_Foto'   => $nombreImg,
                //     'Fecha_Creacion_Foto' => $fechaYhora[0],
                //     'Hora_Creacion_Foto' => $fechaYhora[1],
                // ]);
            }
        }

        echo json_encode(array("status" => true ));
    }

    function eliminarImagenes(){
        // $fotosProblemasMdl = new FotosProblemasMdl();

        $numInspeccion = $this->request->getPost('numInspeccionArchivos');
        $Id_Inspeccion = $this->request->getPost('Id_Inspeccion');

        $data = (get_object_vars(json_decode($this->request->getPost('datosArreglo')))["imgSeleccionadas"]);
        foreach($data as $nombreImg){
            unlink("Archivos_ETIC/inspecciones/".$numInspeccion."/Imagenes/".$nombreImg);
            // $registros = $fotosProblemasMdl->where(["Id_Inspeccion" => $Id_Inspeccion, "Nombre_Foto" => $nombreImg]);
            // $registros->delete();
        }

        echo json_encode(array("status" => true ));
    }

    function explorarArchivos(){
        $numInspeccion = $this->request->getPost('numInspeccionArchivos');

        $ruta = ROOTPATH.'public/Archivos_ETIC/inspecciones/'.$numInspeccion.'/Imagenes';
        $imagenesArray = array();

        // Abre un gestor de directorios para la ruta indicada
        $gestor = opendir($ruta);

        // Recorre todos los elementos del directorio
        while (($archivo = readdir($gestor)) !== false)  {
            // Se muestran todos los archivos y carpetas excepto "." y ".."
            if ($archivo != "." && $archivo != "..") {
                array_push($imagenesArray,$archivo);
            }
        }

        closedir($gestor);
        echo (json_encode($imagenesArray));
    }

    function cambiarEstatusUbicacion($id_insp_det = null, $id_estatus_det = null){
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        $llamadoInterno = false;
        if ($id_insp_det != null && $id_estatus_det != null) {
            $llamadoInterno = true;
        }

        if ($llamadoInterno) {
            $id_inspeccion_det = $id_insp_det;
            $id_estatus_inspeccion_det = $id_estatus_det;
        }else{
            $id_inspeccion_det = $this->request->getPost('Id_Inspeccion_Det');
            $id_estatus_inspeccion_det = $this->request->getPost('idEstatus');
        }
                
        // Si el estatus es igual a POR VERIFICAR entonces se pinta en negro de lo contrario se pinta en color azul
        if ($id_estatus_inspeccion_det == "568798D1-76BB-11D3-82BF-00104BC75DC2") {
            $Id_Estatus_Color_Text = 1;
        }else{
            $Id_Estatus_Color_Text = 4;
        }

        $updateDetalle = $inspeccionesDetMdl->update($id_inspeccion_det,[
            'Id_Status_Inspeccion_Det'=>$id_estatus_inspeccion_det,
            'Id_Estatus_Color_Text'   =>$Id_Estatus_Color_Text,
            'Modificado_Por'          =>$session->Id_Usuario,
            'Fecha_Mod'               =>date("Y-m-d H:i:s")
        ]);

        if($updateDetalle != false){

            $this->actualizarEstatusElementoPadre($id_inspeccion_det);
            if ($llamadoInterno) { return;}

            echo json_encode(array("status" => true ));
        }else{
            echo json_encode(array("status" => false ));
        }
    }

    function actualizarEstatusElementoPadre($id_inspeccion_det){
        $inventariosMdl = new InventariosMdl();
        $inspeccionesDetMdl = new InspeccionesDetMdl();
        $session = session();

        // obtener el id padre del elemento, es el id que se crea por primera vez
        $id_padre = $inventariosMdl->getUbicacionPadre($id_inspeccion_det, $session->Id_Inspeccion)[0]["parent_id"];

        if ($id_padre != "0") {
            $cuantosPorVerificar = $inventariosMdl->cuantosHijosSinInspeccionar($id_padre, $session->Id_Inspeccion);
            $datosDelPadre = $inventariosMdl->getIdInspeccionDetPorIdPadre($id_padre, $session->Id_Inspeccion);
            $idInspeccionDetDelPadre = $datosDelPadre[0]["Id_Inspeccion_Det"];
            $estatusDelPadre = $datosDelPadre[0]["Id_Status_Inspeccion_Det"];
            
            // $estatusIdPadre = $inventariosMdl->getStatusPadre($id_padre, $session->Id_Inspeccion)[0]["Id_Status_Inspeccion_Det"];
            if ($cuantosPorVerificar > 0) {
                $id_status_inspeccion_det = "568798D1-76BB-11D3-82BF-00104BC75DC2"; //estatus por verificado
                $id_color_texto_inspeccion_det = 1; //color texto negro
            }else{
                $id_status_inspeccion_det = "568798D2-76BB-11D3-82BF-00104BC75DC2"; //estatus verificado
                $id_color_texto_inspeccion_det = 4; //color texto azul
            }

            // if ( $estatusDelPadre != '568798D2-76BB-11D3-82BF-00104BC75DC2') {
                # code...
                $updateEstatusPadre = $inspeccionesDetMdl->update($idInspeccionDetDelPadre,[
                    'Id_Status_Inspeccion_Det'=> $id_status_inspeccion_det,
                    'Id_Estatus_Color_Text'   => $id_color_texto_inspeccion_det,
                    'Modificado_Por'          =>$session->Id_Usuario,
                    'Fecha_Mod'               =>date("Y-m-d H:i:s")
                ]);
            // }

            $this->actualizarEstatusElementoPadre($idInspeccionDetDelPadre);
        }

        return;
    }

    function actualizarImgInicial($idInspeccion,$nombreImg,$tipoImg){
        $inspeccionesMdl = new InspeccionesMdl();

        if($tipoImg == 1){
            $updateDetalle = $inspeccionesMdl->update(['Id_Inspeccion' => $idInspeccion],['IR_Imagen_Inicial' => $nombreImg]);
        }else{
            $updateDetalle = $inspeccionesMdl->update(['Id_Inspeccion' => $idInspeccion],['DIG_Imagen_Inicial' => $nombreImg]);
        }

        return;
    }

    public function generarReporteInventarios(){
        $inventariosMdl = new InventariosMdl();
        $inspeccionesMdl = new InspeccionesMdl();
        $problemasMdl = new ProblemasMdl();
        $usuariosMdl = new UsuariosMdl();
        $session = session();

        // Consulta para los datos del analista termografo
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);
        // Problemas de las ubicaciones
        $problemas = $problemasMdl->getProblemas_Sitio_Reporte($session->Id_Sitio,$session->Id_Inspeccion);
        
        // consulta para el inventario de la inspeccion
        $arrayElementosParaReporte = (get_object_vars(json_decode($this->request->getPost('datosArreglo')))["arrayElementosParaReporte"]);
        $ubicacionesInventario = $inventariosMdl->consultaReporte($session->Id_Sitio,$session->Id_Inspeccion,$arrayElementosParaReporte);
        // $ubicacionesInventario = $inventariosMdl->consultaReporte($session->Id_Sitio,$session->Id_Inspeccion,$session->arrayElementosParaReporte);

        $ubicacionesYProblemas = [];
        // Uniendo la consulta de ubicaciones con la de problemas si es que la ubicacion tiene problemas
        foreach($ubicacionesInventario as $key => $value){
            $ubicacionesYProblemas[$key]['Id_Ubicacion'] = $value['Id_Ubicacion'];
            $ubicacionesYProblemas[$key]['level'] = $value['level'];
            $ubicacionesYProblemas[$key]['Estatus'] = $value['Estatus'];
            $ubicacionesYProblemas[$key]['Elemento'] = $value['Elemento'];
            $ubicacionesYProblemas[$key]['Codigo_Barras'] = $value['Codigo_Barras'];
            $ubicacionesYProblemas[$key]['Notas_Inspeccion'] = $value['Notas_Inspeccion'];
            $ubicacionesYProblemas[$key]['Prioridad'] = $value['Prioridad'];
            $ubicacionesYProblemas[$key]['Id_Inspeccion_Det'] = $value['Id_Inspeccion_Det'];
            $ubicacionesYProblemas[$key]['Problemas'] = "";

            foreach($problemas as $key2 => $value2){
                if($value['Id_Ubicacion'] == $value2['Id_Ubicacion']) {
                    $ubicacionesYProblemas[$key]['Problemas'] = $value2['Problemas'];
                }
            }
        }


        $elementos_inventario_ordenados = array();
        for ($i=0; $i < count($arrayElementosParaReporte); $i++) { 
            for ($z=0; $z < count($ubicacionesYProblemas) ; $z++) { 
                if($arrayElementosParaReporte[$i] == $ubicacionesYProblemas[$z]["Id_Inspeccion_Det"]){
                    array_push($elementos_inventario_ordenados,$ubicacionesYProblemas[$z]);
                }
            }
            
        }

        // Ancho de las columnas para tabla
        $w = array(20, 16, 21, 119, 29, 72);
        // Datos para el encabezado del reporte y encaberzado de tabla
        $datosEncabezado = [
            "tipoEncabezado" => 1,
            "titulo" => "Inventario De Equipo",
            "grupo" => "",
            "cliente" => $datosInspeccion[0]['nombreCliente'],
            "sitio" => $datosInspeccion[0]['nombreSitio'],
            "analistaTermografo" => $session->nombre,
            "nivelCertificacion" => $datosInpector[0]['nivelCertificacion'],
            "inspeccionAnterior" => $datosInspeccion[0]['No_Inspeccion_Ant'],
            "fecha_inspeccion_anterior" => $datosInspeccion[0]['fechaInspeccionAnterior'],
            "inspeccionActual" => $datosInspeccion[0]['No_Inspeccion'],
            "fecha_inspeccion_actual" => $datosInspeccion[0]['fechaInspeccionActual'],
            "anchoColumnas" => $w,
        ];

        $pdf = new PDF($datosEncabezado);
        $pdf->AddPage('L','A4',0);

        // Datos de la tabla
        foreach($elementos_inventario_ordenados as $key => $value){
            /* Si es el primer nivel se aplican negritas con la variable $n
            y un margen derecho de 1 para ir identando las ubicaciones */
            if($value['level'] == 1){
                $n = 'B';
                $margenLeft = 1;
            }else{
                $n = '';
                $margenLeft = $value['level'] * 2;
                // $margenLeft = 1;
            }
            /* Recalculamos el ancho de la columna del nombre de ubicacion
            para que no abiente las columnas posteriores,despues de aplicarle el margen dereho */
            $anchoUbicacion = $w[3] - $margenLeft;

            $pdf->SetFont('Arial','',8); $pdf->Cell($w[0],4,utf8_decode($value['Estatus']),0,0,'L');
            $pdf->SetFont('Arial','',8); $pdf->Cell($w[1],4,utf8_decode($value['Prioridad']),0,0,'L');
            $pdf->SetFont('Arial','',8); $pdf->Cell($w[2],4,utf8_decode($value['Problemas']),0,0,'L');
            // Tipo fuente ; identacion para la ubicación ; campo de la consulta;
            $pdf->SetFont('Arial',$n,8); $pdf->Cell($margenLeft); $pdf->Cell($anchoUbicacion,4,utf8_decode($value['Elemento']),0,0,'L');
            $pdf->SetFont('Arial','',8); $pdf->Cell($w[4],4,utf8_decode($value['Codigo_Barras']),0,0,'L');
            $pdf->SetFont('Arial','',8); $pdf->MultiCell($w[5],4,utf8_decode($value['Notas_Inspeccion']),0,'L');
        }
        $pdf->Cell(array_sum($w),0,'','T');

        // Línea de cierre
        $currentTimeinSeconds = time();
        $nombrePdf = 'ETIC_INVENTARIO_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);
        // $pdf->Output('I', $nombrePdf);

        return json_encode(200);
    }

    public function generarReporteProblemas(){
        $inspeccionesMdl = new InspeccionesMdl();
        $usuariosMdl = new UsuariosMdl();
        $problemasMdl = new ProblemasMdl();
        $historialProblemas = new HistorialProblemasMdl();
        $session = session();

        // Consulta para los datos del analista termografo
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);
        // Problemas de las ubicaciones
        $arrayElementosParaReporte = (get_object_vars(json_decode($this->request->getPost('datosArreglo')))["arrayElementosParaReporte"]);

        $condicion = [
            'problemas.Id_Sitio' => $session->Id_Sitio,
            'problemas.Id_Inspeccion' => $session->Id_Inspeccion,
            'problemas.Estatus_Problema' => 'Abierto',
        ];
        $orden = 'problemas.Id_Tipo_Inspeccion ASC, problemas.Numero_Problema ASC';
        $problemas = $problemasMdl->getProblemas_Sitio($condicion, $orden, $arrayElementosParaReporte);

        // Datos para el encabezado del reporte y encaberzado de tabla
        $datosEncabezado = [
            "tipoEncabezado" => 2,
            "titulo" => "",
        ];

        $pdf = new PDF($datosEncabezado);
        $pdf->AddPage('L','A4',0);

        // Contador de iteraciones
        $iteracion = 0;
        // Total de iteraciones para genera paginas
        $totalProblemas = count($problemas) - 1;

        // Datos de la tabla
        foreach($problemas as $key => $value){
            // datos Imagenes
            $imgIR = empty($value['Ir_File']) ? " " : $value['Ir_File'];
            $imgDIG = empty($value['Photo_File']) ? " " : $value['Photo_File'];
            $rutaImgIR = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes_optimizadas/'.$imgIR;
            $rutaImgDIG = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes_optimizadas/'.$imgDIG;
            $ruta_datos_img_ir = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes/'.$imgIR;
            $ruta_datos_img_dig = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes/'.$imgDIG;
            $datosImgIR = $this->obtenerDatosImg(1, $ruta_datos_img_ir);
            $datosImgDIG = $this->obtenerDatosImg(1, $ruta_datos_img_dig);

            // Titulo
            $pdf->SetXY(86,10);
            $pdf->SetFont('Arial','B',13);
            $pdf->MultiCell(125,6,utf8_decode($value['tipoInspeccion']."\nDocumentación Del Problema"),0,'C');

            $pdf->SetY(27);
            // Apartado De datos de la inspeccion
            // if($pdf->datosInspeccion["grupo"] != ""){
            //     $pdf->SetFont('Arial','B',8); $pdf->Cell(80,4,utf8_decode($pdf->datosEncabezado["grupo"]),0,0,'L') ;$pdf->Ln();
            // }
            $pdf->SetFont('Arial','B',8); $pdf->Cell(102,4,utf8_decode($datosInspeccion[0]['nombreCliente']),0,0,'L'); $pdf->Ln();
            $pdf->SetFont('Arial','',8); $pdf->Cell(102,4,utf8_decode($datosInspeccion[0]['nombreSitio']),0,0,'L'); $pdf->Ln();
            $pdf->Cell(102,4,utf8_decode('Analista Termógrafo: '.$session->nombre),0,0,'L'); $pdf->Ln();
            $pdf->Cell(102,4,utf8_decode('Nivel De Certificación: '.$datosInpector[0]['nivelCertificacion']),0,0,'L'); $pdf->Ln();

            if ($value['Id_Tipo_Inspeccion'] != "0D32B333-76C3-11D3-82BF-00104BC75DC2"){
                $pdf->Cell(102,4, 'Fecha De Reporte: '.date("Y/m/d"),0,0,'L'); $pdf->Ln();
                // $pdf->Cell(102,4,utf8_decode('No. Inspección Anterior: '.$datosInspeccion[0]['No_Inspeccion_Ant']),1,0,'L'); $pdf->Ln();
                // $pdf->Cell(102,4,utf8_decode('No. Inspección Actual: '.$datosInspeccion[0]['No_Inspeccion']),1,0,'L');

                // Datos inspeccion actual y anterior
                $pdf->SetFont('Arial','',8); $pdf->Cell(31,4,utf8_decode('No. Inspección Anterior:'),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode($datosInspeccion[0]['No_Inspeccion_Ant']),0,0,'L');$pdf->Ln();
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]["fechaInspeccionAnterior"]),0,0,'L');$pdf->Ln();
                $pdf->SetFont('Arial','',8);$pdf->Cell(31,4,utf8_decode('No. Inspección Actual:'),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode($datosInspeccion[0]['No_Inspeccion']),0,0,'L');$pdf->Ln();
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]["fechaInspeccionActual"]),0,0,'L');$pdf->Ln();
            }

            $historial = array();
            $data = [];

            if(count($problemasMdl->getHistorialProblema($value['Id_Ubicacion'], $value['Id_Tipo_Inspeccion'])) > 0){

                $historial = $problemasMdl->getHistorialProblema($value['Id_Ubicacion'], $value['Id_Tipo_Inspeccion']);
                
                // reordenando los elementos del historial por numero de inspeccion de menor a mayor
                usort($historial, function($a, $b) {
                    return $a['numInspeccion'] - $b['numInspeccion'];
                });

                foreach ($historial as $key5 => $value5) {
                    $data['Problema'][$value5["fecha_problema_historico"]] = $value5["Problem_Temperature"];
                    $data['Referencia'][$value5["fecha_problema_historico"]] = $value5["Reference_Temperature"];
                }

            }
            
            // Antes del proceso DB
            // if ($value['Id_Tipo_Inspeccion'] == 1 || $value['Id_Tipo_Inspeccion'] == 4) {
            // PROBLEMAS ELECTRICOS Y MECANICOS
            if ($value['Id_Tipo_Inspeccion'] == "0D32B331-76C3-11D3-82BF-00104BC75DC2" ||
                $value['Id_Tipo_Inspeccion'] == "0D32B332-76C3-11D3-82BF-00104BC75DC2" ||
                $value['Id_Tipo_Inspeccion'] == "0D32B334-76C3-11D3-82BF-00104BC75DC2") {

                // Calculos de temperatura
                $problemaTemperatura = $value['Problem_Temperature'];
                $temperaturaReferencia = $value['Reference_Temperature'];
                $carga_50 = ceil((($problemaTemperatura - $temperaturaReferencia)* floatval(2.98)) + $temperaturaReferencia);
                $carga_100 = ceil((($problemaTemperatura - $temperaturaReferencia)* floatval(1.00)) + $temperaturaReferencia);

                // Ubicando Apartado con datos del problema
                $pdf->SetXY(10, 67);
                // Apartado de Informacion de temperatura
                $pdf->Rect(10, 67, 67, 38, 'D');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(67,4,utf8_decode('Información De Temperatura'),"B",0,'L'); $pdf->Ln();
                $pdf->SetFont('Arial','',8); $pdf->Cell(22,4,utf8_decode('Temp. Ambiente:'),0,0,'L');
                $Temp_Ambient = $value['Temp_Ambient'] > 0 ? $value['Temp_Ambient']."°C" : " " ;
                $pdf->SetFont('Arial','B',8); $pdf->Cell(45,4,utf8_decode($Temp_Ambient),0,0,'R'); $pdf->Ln();
                $pdf->SetFont('Arial','',8); $pdf->Cell(20,4,utf8_decode('Tipo Ambiente:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(47,4,$value['tipoAmbiente'],0,0,'R'); $pdf->Ln();
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode('Velocidad de Viento:'),0,0,'L');

                $velocidad_viento = $value['Wind_Speed'] != "" && $value['Wind_Speed'] > 0 ? $value['Wind_Speed']." m/s" : " ";
                $pdf->SetFont('Arial','B',8); $pdf->Cell(57,4,$velocidad_viento,0,0,'R'); $pdf->Ln();

                
                $vel_v = floatval($value['Wind_Speed']);
                $temp_prob = floatval($value['Problem_Temperature']);
                $temp_amb = floatval($value['Temp_Ambient']);
                $corriente_nominal = floatval($value['Rated_Load']);

                switch ($vel_v) {
                    case 1:
                        $fcv = 1.15;
                    break;
                    case 2:
                        $fcv = 1.36;
                    break;
                    case 3:
                        $fcv = 1.64;
                    break;
                    case 4:
                        $fcv = 1.86;
                    break;
                    case 5:
                        $fcv = 2.06;
                    break;
                    case 6:
                        $fcv = 2.23;
                    break;
                    case 7:
                        $fcv = 2.40;
                    break;
                    default:
                        $fcv = 2.40;
                    break;
                }

                $ajuste_temperatura_viento = 0;
                if ($value['Wind_Speed'] != "" && $value['Wind_Speed'] > 0) {
                    $ajuste_temperatura_viento = round((($temp_prob - $temp_amb) * (floatval($fcv))) + $temp_amb);
                }else{
                    $ajuste_temperatura_viento = $temp_prob;
                }

                // Tomando el valor maximo entre los rms
                $max_rms_amper_medido = max(floatval($value['Problem_Rms']), floatval($value['Reference_Rms']), floatval($value['Additional_Rms']));
                
                if($corriente_nominal >= 1){
                    $porsentaje_carga = ($max_rms_amper_medido / $corriente_nominal) * 100;
                }else{
                    $porsentaje_carga = 1;
                }

                switch ((round($porsentaje_carga/10)*10)) {
                    case 90:
                        $fcc = 1.20;
                    break;
                    case 80:
                        $fcc = 1.46;
                    break;
                    case 70:
                        $fcc = 1.77;
                    break;
                    case 60:
                        $fcc = 2.27;
                    break;
                    case 50:
                        $fcc = 2.98;
                    break;
                    case 40:
                        $fcc = 4.33;
                    break;
                    default:
                        $fcc = 4.33;
                    break;
                }

                if((round($porsentaje_carga/10)*10) < 40){
                    $fcc = 0;
                }

                $ajuste_temp_carga = round((($ajuste_temperatura_viento - $temp_amb) * (floatval($fcc)))+ $temp_amb);


                $pdf->SetFont('Arial','',8); $pdf->Cell(58,4,utf8_decode('Ajuste de temperatura por viento:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(9,4,utf8_decode($ajuste_temperatura_viento > 0 ? $ajuste_temperatura_viento."°C" : ""),0,0,'R'); $pdf->Ln();
                $pdf->SetFont('Arial','',8); $pdf->Cell(58,4,utf8_decode('Temperatura ajuste por carga:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(9,4,utf8_decode($ajuste_temp_carga."°C"),0,0,'R');
                // $pdf->SetFont('Arial','',8); $pdf->Cell(58,4,utf8_decode('Temperatura estimada 100%:'),0,0,'L');
                // $pdf->SetFont('Arial','B',8); $pdf->Cell(9,4,utf8_decode($proyeccion_100."°C"),0,0,'R'); $pdf->Ln();

                // Ubicando Apartado Equipment Information
                $pdf->SetXY(10,108);
                $pdf->Rect(10, 108, 67, 38, 'D');
                $pdf->SetX(10);
                $pdf->SetFont('Arial','B',8); $pdf->Cell(67,4,utf8_decode('Información Del Equipo'),'B',0,'L'); $pdf->Ln();
                $pdf->SetX(10);
                $pdf->SetFont('Arial','',8); $pdf->Cell(25,4,utf8_decode('Componente:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(42,4,'INT',0,0,'L'); $pdf->Ln();
                $pdf->SetX(10); $pdf->SetFont('Arial','',8); $pdf->Cell(25,4,utf8_decode('Tipo Falla:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(42,4,utf8_decode($value['tipoInspeccion']),0,0,'L'); $pdf->Ln();
                $pdf->SetX(10); $pdf->SetFont('Arial','',8); $pdf->Cell(25,4,utf8_decode('Fabricante:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(42,4,utf8_decode($value['fabricante']),0,0,'L'); $pdf->Ln();
                $pdf->SetX(10); $pdf->SetFont('Arial','',8); $pdf->Cell(29,4,utf8_decode('Circuito Voltage [V]:'),0,0,'L');

                $voltage = $value['Circuit_Voltage'] > 0 ? $value['Circuit_Voltage']." V" : " " ;
                $pdf->SetFont('Arial','B',8);
                $pdf->Cell(38,4,$voltage,0,0,'L'); $pdf->Ln();

                $pdf->SetTextColor(0,35,172);
                $pdf->SetX(10);
                $pdf->SetFont('Arial','',8);
                $pdf->Cell(29,4,utf8_decode('Corriente Nominal [A]:'),0,0,'L');

                $amperios = $value['Rated_Load'] > 0 ? $value['Rated_Load']." A" : " " ;
                $pdf->SetFont('Arial','B',8);
                $pdf->Cell(38,4,$amperios,0,0,'L'); $pdf->Ln();

                // Apartado RMS FASES Load Test Results
                $pdf->SetXY(10,149);
                $pdf->Rect(10, 149, 67, 38, 'D');
                $pdf->SetTextColor(0,0,0);
                $pdf->SetFont('Arial','B',8); $pdf->Cell(67,4,utf8_decode('Datos De Medición De Carga'),'B',0,'L'); $pdf->Ln();

                $pdf->SetTextColor(0,35,172);
                $pdf->SetX(10); $pdf->SetFont('Arial','',8); $pdf->MultiCell(25,4,utf8_decode('RMS Amperes:'),0,'L');
                $pdf->SetXY(35,153); $pdf->SetFont('Arial','B',8); $pdf->MultiCell(42,4,"",0,'L'); $pdf->Ln();

                $pdf->SetTextColor(0,0,0);
                $faseProblema = $value['faseProblema'] != "" ? $value['faseProblema'].':' : " " ;
                $Problem_Rms = $value['Problem_Rms'] > 0 ? $value['Problem_Rms'] : " " ;
                $pdf->SetXY(10,157); $pdf->SetFont('Arial','',8); $pdf->MultiCell(25,4,utf8_decode($faseProblema),0,'L');
                $pdf->SetXY(35,157); $pdf->SetTextColor(0,35,172);$pdf->SetFont('Arial','B',8); $pdf->MultiCell(42,4,$Problem_Rms,0,'L'); $pdf->Ln();

                $pdf->SetTextColor(0,0,0);
                $faseReferencia = $value['faseReferencia'] != "" ? $value['faseReferencia'].':' : " " ;
                $Reference_Rms = $value['Reference_Rms'] > 0 ? $value['Reference_Rms'] : " " ;
                $pdf->SetXY(10,161); $pdf->SetFont('Arial','',8); $pdf->MultiCell(25,4,utf8_decode($faseReferencia),0,'L');
                $pdf->SetXY(35,161); $pdf->SetTextColor(0,35,172);$pdf->SetFont('Arial','B',8); $pdf->MultiCell(42,4,$Reference_Rms,0,'L'); $pdf->Ln();

                $pdf->SetTextColor(0,0,0);
                $faseAdicional = $value['faseAdicional'] != "" ? $value['faseAdicional'].':' : " " ;
                $Additional_Rms = $value['Additional_Rms'] > 0 ? $value['Additional_Rms'] : " " ;
                $pdf->SetXY(10,165); $pdf->SetFont('Arial','',8); $pdf->MultiCell(25,4,utf8_decode($faseAdicional),0,'L');
                $pdf->SetXY(35,165); $pdf->SetTextColor(0,35,172);$pdf->SetFont('Arial','B',8); $pdf->MultiCell(42,4,$Additional_Rms,0,'L'); $pdf->Ln();

                $pdf->SetTextColor(0,0,0);
                $pdf->SetXY(10,177); $pdf->SetFont('Arial','',8); $pdf->MultiCell(25,4,utf8_decode('Emisividad:'),0,'L');
                $Emissivity = $value['Emissivity'] > 0 ? $value['Emissivity'] : " " ;
                $pdf->SetXY(35,177); $pdf->SetFont('Arial','B',8); $pdf->MultiCell(42,4,$Emissivity,0,'L'); $pdf->Ln();

                // Apartado DATOS PROBELMA
                $pdf->SetXY(246,23);
                $pdf->SetFont('Arial','',8); $pdf->Cell(19,4,'Problema No:','LT',0,'R');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(22,4,utf8_decode($value['tipoInspeccion'].' / '.$value['Numero_Problema']),'TR',0,'L'); $pdf->Ln();
                $pdf->SetXY(185,27);
                $pdf->SetTextColor(0,0,0);
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode('Es Crónico:'),'LT',0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(87,4,$value['Es_Cronico'],'TR',0,'L'); $pdf->Ln();
                $pdf->SetXY(185,31);
                $pdf->SetFont('Arial','',8); $pdf->Cell(27,4,utf8_decode('Prioridad Operación:'),'L',0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->MultiCell(75,4,utf8_decode($value['tipoPrioridad']),'R','L');
                $pdf->SetXY(185,35);
                $pdf->SetFont('Arial','',8); $pdf->Cell(33,4,utf8_decode('Prioridad De Reparación:'),'L',0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(69,4,utf8_decode($value['severidad']),'R',0,'L'); $pdf->Ln();
                $pdf->SetXY(185,39);
                $pdf->SetFont('Arial','',8); $pdf->MultiCell(85,4,utf8_decode('Temperatura De Anomalía En '.$value['faseProblema'].':'),'L','L');
                $pdf->SetXY(270,39);
                $pdf->SetFont('Arial','B',8); $pdf->MultiCell(17,4,utf8_decode($value['Problem_Temperature'].'°C'),'R','R');
                $pdf->SetXY(185,43);
                $pdf->SetTextColor(0,35,172); $pdf->SetFont('Arial','',8); $pdf->MultiCell(85,4,utf8_decode('Temperatura De Referencia En: '.$value['faseReferencia']),'L','L');
                $pdf->SetXY(270,43);
                $pdf->SetFont('Arial','B',8); $pdf->MultiCell(17,4,utf8_decode($value['Reference_Temperature'].'°C'),'R','R');
                $pdf->SetXY(185,47);
                $pdf->SetTextColor(245,0,0); $pdf->SetFont('Arial','',8); $pdf->Cell(85,4,utf8_decode('Diferencial De temperatura:'),'LB',0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(17,4,utf8_decode($value['Aumento_Temperatura'].'°C'),'RB',0,'R'); $pdf->Ln();

                // Apartado de informacion del equipo
                $pdf->SetTextColor(0,0,0);
                $pdf->Rect(185, 54, 102, 48, 'D');
                $pdf->SetXY(185,54);
                $pdf->SetFont('Arial','B',8); $pdf->Cell(102,4,utf8_decode('Información Del Equipo'),'B',0,'L'); $pdf->Ln();
                $pdf->SetX(185); $pdf->SetFont('Arial','',8); $pdf->Cell(102,4,utf8_decode('Código De Barras: '.$value['codigoBarras']),0,0,'L'); $pdf->Ln();

                $arrayRuta = explode(" / ", $value['Ruta']);

                if (count($arrayRuta) > 6) {
                    $pdf->SetX(185); $pdf->SetFont('Arial','',8); $pdf->MultiCell(102,4,utf8_decode($value['Ruta']),0,'L');
                }else{

                    $identar = "";
                    foreach($arrayRuta as $valueRuta){
                        $pdf->SetX(185); $pdf->SetFont('Arial','',8); $pdf->Cell(102,4,utf8_decode($identar.$valueRuta),0,0,'L'); $pdf->Ln();
                        $identar = $identar."\r\n \r\n";
                    }
                }

                // Comentarios del problema
                $pdf->SetXY(185,86);
                $pdf->SetTextColor(0,35,172);
                $pdf->MultiCell(102,4,utf8_decode($value['Component_Comment']),0,'L');
                $pdf->SetTextColor(0,0,0);

                // GRafica
                if ($value['Id_Tipo_Inspeccion'] == "0D32B331-76C3-11D3-82BF-00104BC75DC2"){
                    $data['Problema'][$value['fecha_key_grafica']] = $problemaTemperatura;
                    // $data['Problema']['50%'] = $carga_50;
                    // $data['Problema']['100%'] = $carga_100;

                    $data["Referencia"][$value['fecha_key_grafica']] = $temperaturaReferencia;
                    // $data["Referencia"]['50%'] = $temperaturaReferencia;
                    // $data["Referencia"]['100%'] = $temperaturaReferencia;

                }else{
                    $data['Problema'][$value['fecha_key_grafica']] = $problemaTemperatura;
                    $data["Referencia"][$value['fecha_key_grafica']] = $temperaturaReferencia;
                }
                $colors = array(
                    'Problema' => array(245,0,0),
                    'Referencia' => array(15,142,149),
                );

                $pdf->SetXY(75,33);
                $pdf->LineGraph(105,69,$data,'VHkBgBdB',$colors,0,10);

                // Imagenes
                if (file_exists($rutaImgIR)){
                    $pdf->Image(base_url($rutaImgIR),80,105,102);
                }
                else{
                    $pdf->Rect(80, 105, 102, 76, 'D');
                    $pdf->SetXY(121,141);
                    $pdf->Cell(20,4,utf8_decode("Sin Imagen"),0,0,"C");
                }

                if(file_exists($rutaImgDIG)){
                    $pdf->Image(base_url($rutaImgDIG),185,105,102);
                }else{
                    $pdf->Rect(185, 105, 102, 76, 'D');
                    $pdf->SetXY(226,141);
                    $pdf->Cell(20,4,utf8_decode("Sin Imagen"),0,0,"C");
                }

                // Apartado DAtos imagenes
                $pdf->SetXY(80,183);
                $pdf->SetTextColor(0,0,0);
                $pdf->SetFont('Arial','',8);
                $pdf->Cell(41,4,'Archivo: '.utf8_decode($imgIR),'TLB',0,'L');
                $pdf->Cell(33,4,'Fecha: '.$datosImgIR['fecha'],'BT',0,'L');
                $pdf->Cell(28,4,'Hora: '.$datosImgIR['hora'],'TRB',0,'L');
                $pdf->SetXY(185,183);
                $pdf->Cell(41,4,'Archivo: '.utf8_decode($imgDIG),'TLB',0,'L');
                $pdf->Cell(33,4,'Fecha: '.$datosImgDIG['fecha'],'BT',0,'L');
                $pdf->Cell(28,4,'Hora: '.$datosImgDIG['hora'],'TRB',0,'L');

            // Antes del proceso DB
            // }elseif ($value['Id_Tipo_Inspeccion'] == 3){
            // PROBLEMAS VISUALES
            }elseif ($value['Id_Tipo_Inspeccion'] == "0D32B333-76C3-11D3-82BF-00104BC75DC2"){

                // Apartado de informacion del equipo
                $pdf->SetTextColor(0,0,0);
                $pdf->Rect(113, 27, 80, 30, 'D');
                $pdf->SetXY(113,27);
                $pdf->SetFont('Arial','B',8); $pdf->Cell(80,4,utf8_decode('Ubicación Del Equipo'),'B',0,'L'); $pdf->Ln();
                $pdf->SetX(113); $pdf->SetFont('Arial','',8); $pdf->Cell(80,4,utf8_decode('Código De Barras: '.$value['codigoBarras']),0,0,'L'); $pdf->Ln();

                $arrayRuta = explode(" / ", $value['Ruta']);
                if (count($arrayRuta) > 5) {
                    $pdf->SetX(113); $pdf->SetFont('Arial','',8); $pdf->MultiCell(80,4,utf8_decode($value['Ruta']),0,'L');
                }else{

                    $identar = "";
                    foreach($arrayRuta as $valueRuta){
                        $pdf->SetX(113); $pdf->SetFont('Arial','',8); $pdf->Cell(80,4,utf8_decode($identar.$valueRuta),0,0,'L'); $pdf->Ln();
                        $identar = $identar."\r\n \r\n";
                    }
                }

                // Apartado DATOS PROBELMA
                $pdf->SetXY(195,23);
                $pdf->Rect(195, 27, 92, 30, 'D');
                $pdf->SetTextColor(0,0,0);

                $pdf->SetFont('Arial','',8); $pdf->Cell(45,4,'',0,0,'R');
                $pdf->SetFont('Arial','',8); $pdf->Cell(25,4,'Problema No:','LT',0,'R');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(22,4,utf8_decode($value['tipoInspeccion'].' / '.$value['Numero_Problema']),'TR',0,'L'); $pdf->Ln();

                $pdf->SetX(195); $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode('Es Crónico:'),"B",0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(77,4,$value['Es_Cronico'],"B",0,'L'); $pdf->Ln();

                $pdf->SetX(195); $pdf->SetFont('Arial','',8); $pdf->Cell(27,4,utf8_decode('Prioridad Operación:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->MultiCell(65,4,utf8_decode($value['tipoPrioridad']),0,'L');

                $pdf->SetX(195); $pdf->SetFont('Arial','',8); $pdf->Cell(33,4,utf8_decode('Prioridad De Reparación:'),0,0,'L');
                $pdf->SetFont('Arial','B',8); $pdf->Cell(59,4,utf8_decode($value['severidad']),0,0,'L'); $pdf->Ln();

                $pdf->SetFont('Arial','',8);
                $pdf->SetX(195); $pdf->Cell(92,4, 'Fecha De Reporte: '.date("Y/m/d"),0,0,'L'); $pdf->Ln();
                // $pdf->SetX(195); $pdf->Cell(102,4,utf8_decode('No. Inspección Anterior: '.$datosInspeccion[0]['No_Inspeccion_Ant']),0,0,'L'); $pdf->Ln();
                // $pdf->SetX(195); $pdf->Cell(102,4,utf8_decode('No. Inspección Actual: '.$datosInspeccion[0]['No_Inspeccion']),0,0,'L');

                // Datos inspeccion actual y anterior
                $pdf->SetX(195);
                $pdf->SetFont('Arial','',8); $pdf->Cell(31,4,utf8_decode('No. Inspección Anterior:'),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]['No_Inspeccion_Ant']),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]["fechaInspeccionAnterior"]),0,0,'L');$pdf->Ln();
                $pdf->SetX(195);
                $pdf->SetFont('Arial','',8);$pdf->Cell(31,4,utf8_decode('No. Inspección Actual:'),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]['No_Inspeccion']),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
                $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]["fechaInspeccionActual"]),0,0,'L');$pdf->Ln();

                // partado datos del problema
                // $pdf->Rect(10, 67, 276, 24, 'D');
                $pdf->SetY(69);
                $pdf->SetFont('Arial','B',8); $pdf->Cell(30,4,utf8_decode("Hallazgo Visual:"),0,0,"R");
                // $pdf->SetFont('Arial','',8);  $pdf->Cell(246,4,utf8_decode($value["hazardGroup"]),0,0,"L"); $pdf->Ln();
                // $pdf->SetFont('Arial','B',8); $pdf->Cell(30,4,utf8_decode("Descripción:"),0,0,"R");
                $pdf->SetFont('Arial','',8);  $pdf->Cell(246,4,utf8_decode($value["hazardIssue"]),0,0,"L"); $pdf->Ln();
                $pdf->SetFont('Arial','B',8); $pdf->Cell(30,4,utf8_decode("Observaciones:"),0,0,"R");
                $pdf->SetFont('Arial','',8);  $pdf->SetTextColor(0,35,172); $pdf->MultiCell(246,4,utf8_decode($value["Component_Comment"]),0,"L"); $pdf->Ln();
                $pdf->SetTextColor(0,0,0);

                // Imagenes
                if (file_exists($rutaImgIR)){
                    $pdf->Image(base_url($rutaImgIR),26,93,121);
                }
                else{
                    $pdf->Rect(26, 93, 121, 90, 'D');
                    $pdf->SetXY(76,136);
                    $pdf->Cell(20,4,utf8_decode("Sin Imagen"),0,0,"C");
                }

                if(file_exists($rutaImgDIG)){
                    $pdf->Image(base_url($rutaImgDIG),150,93,121);
                }else{
                    $pdf->Rect(150, 93, 121, 90, 'D');
                    $pdf->SetXY(200,136);
                    $pdf->Cell(20,4,utf8_decode("Sin Imagen"),0,0,"C");
                }

                // Apartado DAtos imagenes
                $pdf->SetXY(26,185);
                $pdf->SetTextColor(0,0,0);
                $pdf->SetFont('Arial','',8);
                $pdf->Cell(53,4,'Archivo: '.utf8_decode($imgIR),'TLB',0,'L');
                $pdf->Cell(44,4,'Fecha: '.$datosImgIR['fecha'],'BT',0,'L');
                $pdf->Cell(24,4,'Hora: '.$datosImgIR['hora'],'TRB',0,'L');
                $pdf->SetXY(150,185);
                $pdf->Cell(53,4,'Archivo: '.utf8_decode($imgDIG),'TLB',0,'L');
                $pdf->Cell(44,4,'Fecha: '.$datosImgDIG['fecha'],'BT',0,'L');
                $pdf->Cell(24,4,'Hora: '.$datosImgDIG['hora'],'TRB',0,'L');

            }

            if($iteracion < $totalProblemas){
                $pdf->AddPage('L','A4',0);
            }

            $iteracion = $iteracion + 1;
        }

        // Línea de cierre
        $currentTimeinSeconds = time();
        $nombrePdf = $this->request->getPost('numero_reporte').'_ETIC_PROBLEMAS_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);
        // $pdf->Output('I', $nombrePdf);

        return json_encode(200);
    }

    public function ordenar_ubicaciones_baseline(){
        $lineaBaseMdl = new LineaBaseMdl();
        $session = session();
        $db = db_connect();

        // arreglo con las ubicaciones ordenadas en el js
        $orden_ubicaciones = $this->request->getPost('orden_ubicaciones');

        // Base LIne unicamente de la inspeccion
        $baseLine = $lineaBaseMdl->getHistorialBaseLine($session->Id_Inspeccion);
        $array_ids = array();
        $elementos_inventario_ordenados = array();
        for ($i=0; $i < count($orden_ubicaciones); $i++) { 
            for ($z=0; $z < count($baseLine) ; $z++) { 
                if($baseLine[$z]["Id_Ubicacion"] == $orden_ubicaciones[$i]){
                    array_push($array_ids,'"'.$baseLine[$z]["Id_Ubicacion"].'"');
                    array_push($elementos_inventario_ordenados,$baseLine[$z]);
                }
            }
        }

        $str = implode(",", $array_ids);
        $str = "(".$str.")";
        
        if (count($array_ids)>0) {
            $todo_el_historial = $db->query("SELECT * FROM v_historial_baseline WHERE Id_Ubicacion IN $str")->getResult('array');
        }else{
            $todo_el_historial = array();
        }
        
        $arr_hist = array();
        for ($i=0; $i < count($elementos_inventario_ordenados); $i++) { 
            for ($z=0; $z < count($todo_el_historial); $z++) { 
                if($elementos_inventario_ordenados[$i]["Id_Ubicacion"] == $todo_el_historial[$z]["Id_Ubicacion"]){
                    array_push($arr_hist,$todo_el_historial[$z]);
                }
            }
            $elementos_inventario_ordenados[$i]["historia"] = $arr_hist;
            $arr_hist = array();
        }

        return json_encode($elementos_inventario_ordenados);
    }

    public function generarReporteBaseLine(){
        $inspeccionesMdl = new InspeccionesMdl();
        $lineaBaseMdl = new LineaBaseMdl();
        $usuariosMdl = new UsuariosMdl();
        $session = session();

        // arreglo con las ubicaciones ordenadas en el js
        $baseLine = $this->request->getPost('orden_ubicaciones');
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);

        // Datos para el encabezado del reporte y encaberzado de tabla
        $datosEncabezado = [
            "tipoEncabezado" => 3,
            "titulo" => "",
        ];

        $pdf = new PDF($datosEncabezado);
        $pdf->AliasNbPages();
        $pdf->AddPage('L','A4',0);

        // Contador de iteraciones
        $iteracion = 0;
        // Total de iteraciones para genera paginas
        $totalBaseLine = count($baseLine) - 1;
        // Datos de la tabla
        foreach($baseLine as $key => $value){

            // Ruta Imagenes
            $rutaImgError = "img/sistema/imagen-no-disponible.jpeg";
            $imgIR = empty($value['Archivo_IR']) ? " " : $value['Archivo_IR'];
            $imgDIG = empty($value['Archivo_ID']) ? " " : $value['Archivo_ID'];
            $rutaImgIR = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes_optimizadas/'.$imgIR;
            $rutaImgDIG = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes_optimizadas/'.$imgDIG;
            $ruta_datos_img_ir = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes/'.$imgIR;
            $ruta_datos_img_dig = 'Archivos_ETIC/inspecciones/'.$value['numInspeccion'].'/Imagenes/'.$imgDIG;
            $datosImgIR = $this->obtenerDatosImg(1, $ruta_datos_img_ir);
            $datosImgDIG = $this->obtenerDatosImg(1, $ruta_datos_img_dig);

            // Título
            $pdf->SetXY(86,10);
            $pdf->SetFont('Arial','B',13);
            $pdf->MultiCell(125,6,utf8_decode("Baseline Equipo En Monitoreo\nInforme de Tendencias"),0,'C');

            $pdf->SetY(27);
            // Apartado De datos de la inspeccion
            // if($pdf->datosInspeccion["grupo"] != ""){
            //     $pdf->SetFont('Arial','B',8); $pdf->Cell(80,4,utf8_decode($pdf->datosEncabezado["grupo"]),0,0,'L') ;$pdf->Ln();
            // }
            $pdf->SetFont('Arial','B',8); $pdf->Cell(85,4,utf8_decode($datosInspeccion[0]['nombreCliente']),0,0,'L'); $pdf->Ln();
            $pdf->SetFont('Arial','',8); $pdf->Cell(85,4,utf8_decode($datosInspeccion[0]['nombreSitio']),0,0,'L'); $pdf->Ln();
            $pdf->Cell(85,4,utf8_decode('Analista Termógrafo: '.$session->nombre),0,0,'L'); $pdf->Ln();
            $pdf->Cell(85,4,utf8_decode('Nivel De Certificación: '.$datosInpector[0]['nivelCertificacion']),0,0,'L'); $pdf->Ln();
            $pdf->Cell(85,4, 'Fecha De Reporte: '.date("Y/m/d"),0,0,'L'); $pdf->Ln();

            // Datos inspeccion actual y anterior
            $pdf->SetXY(95, 27);
            $pdf->SetFont('Arial','B',8); $pdf->Cell(34,4,utf8_decode('No. Inspección Anterior:'),0,0,'R');
            $pdf->SetFont('Arial','',8); $pdf->Cell(11,4,utf8_decode($datosInspeccion[0]['No_Inspeccion_Ant']),0,0,'L');
            $pdf->SetFont('Arial','B',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'C');
            $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]['fechaInspeccionAnterior']),0,0,'C'); $pdf->Ln();

            $pdf->SetFont('Arial','B',8); $pdf->SetX(95); $pdf->Cell(34,4,utf8_decode('No. Inspección Actual:'),0,0,'R');
            $pdf->SetFont('Arial','',8); $pdf->Cell(11,4,utf8_decode($datosInspeccion[0]['No_Inspeccion']),0,0,'L');
            $pdf->SetFont('Arial','B',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'C');
            $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]['fechaInspeccionActual']),0,0,'C'); $pdf->Ln();

            // Apartado de informacion del equipo
            $pdf->SetTextColor(0,0,0);
            // $pdf->Rect(174, 25, 61, 12, 'D');
            $pdf->SetXY(175,27);
            $pdf->SetFont('Arial','B',8); $pdf->Cell(63,4,utf8_decode('Información Del Equipo'),1,0,'L'); $pdf->Ln();
            $pdf->SetX(175); $pdf->SetFont('Arial','B',8); $pdf->Cell(26,4,utf8_decode('Código De Barras: '),"L",0,'L');
            $pdf->SetFont('Arial','',8); $pdf->Cell(37,4,utf8_decode($value['codigoBarras']),"R",0,'R'); $pdf->Ln();

            $pdf->SetX(175); $pdf->SetFont('Arial','B',8); $pdf->Cell(17,4,utf8_decode('Fabricante:'),"L",0,'L');
            $pdf->SetFont('Arial','',8); $pdf->Cell(46,4,utf8_decode($value['fabricante']),"R",0,'L'); $pdf->Ln();
            
            $pdf->SetX(175); $pdf->SetFont('Arial','B',8); $pdf->Cell(30,4,utf8_decode('Prioridad Operación:'),"LB",0,'L');
            $pdf->SetFont('Arial','',8); $pdf->Cell(33,4,utf8_decode($value['tipoPrioridad']),"RB",0,'L'); $pdf->Ln();

            // Recuadro de Prioridad Operatica
            $pdf->SetXY(247, 27);
            $pdf->SetFont('Arial','B',8); $pdf->Cell(40,4,'Prioridad Operativa',1,0,'L'); $pdf->Ln();
            $pdf->SetX(247);$pdf->SetFont('Arial','',8); $pdf->Cell(40,4,utf8_decode('CTO = Crítico'),'L,R',0,'L'); $pdf->Ln();
            $pdf->SetX(247);$pdf->Cell(40,4,utf8_decode('ETO = Esencial'),'L,R',0,'L'); $pdf->Ln();
            $pdf->SetX(247);$pdf->Cell(40,4,'UN = No clasificado','L,R,B',0,'L'); $pdf->Ln();

            // Ruta
            $pdf->SetY(56);
            $pdf->SetFont('Arial','B',8); $pdf->Cell(277,4,utf8_decode('RUTA: '.$value['path']),'B',0,'L'); $pdf->Ln();
            $pdf->SetFont('Arial','',8);

            // Imagenes
            if (file_exists($rutaImgIR)){
                $pdf->Image(base_url($rutaImgIR),10,64,90);
            }else{
                $pdf->Rect(10,64,90,67,'D');
                $pdf->SetXY(46,92);
                $pdf->Cell(20,4,utf8_decode("Sin Imagen"),0,0,"C");
            }

            if(file_exists($rutaImgDIG)){
                $pdf->Image(base_url($rutaImgDIG),104,64,90);
            }else{
                $pdf->Rect(104,64,90,67,'D');
                $pdf->SetXY(136,92);
                $pdf->Cell(20,4,utf8_decode("Sin Imagen"),0,0,"C");
            }

            // GRafica
            $pdf->SetFont('Arial','',5);
            $valoresTemperaturas = array();
            // $historialBaseLine = $lineaBaseMdl->getHistorialBaseLine($value['Id_Ubicacion'],"");
            $historialBaseLine = $value['historia'];

            // Uniendo la consulta de ubicaciones con la de problemas si es que la ubicacion tiene problemas
            if (count($historialBaseLine) > 5) {
                $historialBaseLine_grafica = array_slice($historialBaseLine, -5);
            }else{
                $historialBaseLine_grafica = $historialBaseLine;
            }

            foreach($historialBaseLine_grafica as $key2 => $value2){
                // Se agregó el numero de inspeccion a la fecha para que ksrot los ordenara correctamente
                // cuando se pinta la grafica, ahí se quita el numero de inspeccion y solo se pinta la fecha
                $mta[$value2["numInspeccion"]."-".$value2['fechaInspeccion']] = $value2['MTA'];
                $max[$value2["numInspeccion"]."-".$value2['fechaInspeccion']] = $value2['Temp_max'];
                $amb[$value2["numInspeccion"]."-".$value2['fechaInspeccion']] = $value2['Temp_amb'];
                array_push($valoresTemperaturas,$value2['Temp_max'],$value2['MTA'],$value2['Temp_amb']);
            }
            // ordenamos los elementos del arreglo de forma ascendente para la grafica
            ksort($max);
            ksort($mta);
            ksort($amb);
            // print_r($data);
            // return
            $data = array('Temp Amb' => $amb, 'MTA' => $max, 'Temp Máx' => $mta);
            $colors = array(
                'Temp Amb' => array(23,49,182),
                'MTA' => array(245,0,0),
                'Temp Máx' => array(15,142,149),
            );

            // Limpiando los arreglos de las temperaturas para que no pinten temperaturas de otros registros
            $max = array();
            $mta = array();
            $amb = array();

            // unimos los valores de las temperaturas y las ordenamos para obtener la mas alta y le sumamos 5
            arsort($valoresTemperaturas);
            $maxTemperaturas = array();
            foreach ($valoresTemperaturas as $key3 => $value3) {
                array_push($maxTemperaturas,$value3);
            }
            $maxValoresY = $maxTemperaturas[0];
            /* Si tiene valor cero marca error y truena por eso se valida y asigna un valorpor defecto que debe ser igual a las diviciones */
            if($maxTemperaturas[0] == 0){
                $maxValoresY = 10;
            }
            $diviciones = 10;

            $pdf->SetXY(193, 70);
            $pdf->LineGraph(92,67,$data,'VHkBgBdB',$colors,$maxValoresY,$diviciones);

            // Apartado DAtos imagenes
            $pdf->SetXY(10,133);
            $pdf->SetTextColor(0,0,0);
            $pdf->SetFont('Arial','',8);
            $pdf->Cell(37,4,'Archivo: '.utf8_decode($imgIR),'TLB',0,'L');
            $pdf->Cell(30,4,'Fecha: '.$datosImgIR['fecha'],'BT',0,'L');
            $pdf->Cell(23,4,'Hora: '.$datosImgIR['hora'],'TRB',0,'L');
            $pdf->SetXY(104,133);
            $pdf->Cell(37,4,'Archivo: '.utf8_decode($imgDIG),'TLB',0,'L');
            $pdf->Cell(30,4,'Fecha: '.$datosImgDIG['fecha'],'BT',0,'L');
            $pdf->Cell(23,4,'Hora: '.$datosImgDIG['hora'],'TRB',0,'L');

            // Ubicando la tabla XY
            $pdf->SetXY(10, 150);
            // Encabezados de la tabla
            $header = array('No. Inspección', 'Fecha Inspección','Estatus', 'T° max', 'MTA', 'T° amb','Notas');
            // Ancho de las columnas 277
            $w = array(24, 27, 18, 12, 11, 12, 173);
            $alineacion_tabla_header = array("L","C","C","C","C","C","L");
            $alineacion_tabla = array("L","C","C","C","C","C","L");

            // Encabezados de la tabala con Negritas
            $pdf->SetFont('Arial','B',8);
            for($i=0;$i<count($header);$i++)
                $pdf->Cell($w[$i],4,utf8_decode($header[$i]),'B',0,$alineacion_tabla_header[$i]);
            $pdf->Ln();

            foreach($historialBaseLine as $keyBL => $valueBL){
                $pdf->SetFont('Arial','',8); $pdf->Cell($w[0],4,utf8_decode($valueBL['numInspeccion']),0,0,$alineacion_tabla[0]);
                // $pdf->SetFont('Arial','',8); $pdf->Cell($w[1],4,utf8_decode($valueBL['Fecha_Creacion']),0,0,$alineacion_tabla[]);
                $pdf->SetFont('Arial','',8); $pdf->Cell($w[1],4,utf8_decode($valueBL['fechaInspeccion']),0,0,$alineacion_tabla[1]);
                $pdf->SetFont('Arial','',8); $pdf->Cell($w[2],4,utf8_decode($valueBL['estatusInspeccion']),0,0,$alineacion_tabla[2]);
                $pdf->SetFont('Arial','',8); $pdf->Cell($w[3],4,utf8_decode($valueBL['MTA']."°C"),0,0,$alineacion_tabla[3]);
                $pdf->SetFont('Arial','',8); $pdf->Cell($w[4],4,utf8_decode($valueBL['Temp_max']."°C"),0,0,$alineacion_tabla[4]);
                $pdf->SetFont('Arial','',8); $pdf->Cell($w[5],4,utf8_decode($valueBL['Temp_amb']."°C"),0,0,$alineacion_tabla[5]);
                $pdf->SetFont('Arial','',8); $pdf->MultiCell($w[6],4,utf8_decode($valueBL['Notas']),0,$alineacion_tabla[6]);
            }

            if($iteracion < $totalBaseLine){
                $pdf->AddPage('L','A4',0);
            }

            $iteracion = $iteracion + 1;
        }

        // Línea de cierre
        
        $nombrePdf = $this->request->getPost('numero_reporte').'_ETIC_BASELINE_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);
        // $pdf->Output('I', $nombrePdf);

        return json_encode(200);
    }

    public function unir_reportes_baseline_problemas(){
        $session = session();

        $ruta = ROOTPATH."public/Archivos_ETIC/inspecciones/".$session->inspeccion."/Reportes/";

        if ($this->request->getPost('reporte') == "baseline"){
            $nombre_reporte ="ETIC_BASELINE_INSPECCION_";
        }else{
            $nombre_reporte ="ETIC_PROBLEMAS_INSPECCION_";
        }

        $files = array();
        for ($i=1; $i <= $this->request->getPost('numero_de_reportes') ; $i++) { 
            array_push($files,$ruta.$i."_".$nombre_reporte.$session->inspeccion.".pdf");
        }

        $pdf = new Fpdi();
        foreach ($files as $file) {
            $pageCount = $pdf->setSourceFile($file);
            for ($pageNo=1; $pageNo <= $pageCount; $pageNo++) { 
                $template = $pdf->importPage($pageNo);
                $size = $pdf->getTemplateSize($template);
                $pdf->AddPage($size["orientation"], $size);
                $pdf->useTemplate($template);
            }
        }
        
        $nombrePdf = $nombre_reporte.$session->inspeccion.'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$session->inspeccion.'/Reportes/'.$nombrePdf);
        
        return json_encode(200);
    }

    public function generarReporteListaProblemas($estatus){
        $inventariosMdl = new InventariosMdl();
        $inspeccionesMdl = new InspeccionesMdl();
        $problemasMdl = new ProblemasMdl();
        $usuariosMdl = new UsuariosMdl();
        $session = session();

        // Consulta para los datos del analista termografo
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);
        // Problemas de las ubicaciones
        $problemas = $problemasMdl->getReporteListaProblemas($session->Id_Sitio,$estatus,$session->Id_Inspeccion);

        // Ancho de las columnas
        $w = array(140,19,22,17,17,14,15,15,18);
        // Alineacion de las columnas
        $alineacion = array("L","C","C","L","C","C","C","C","C");

        // iMAGEN DEL CLIENTE
        // Obteniendo el nombre y ruta de la imagen del cliente
        $imagen_cliente = $datosInspeccion[0]['imagen_cliente'];
        $ruta_imagen_cliente = 'Archivos_ETIC/clientes_img/'.$imagen_cliente;
        
        // Datos para el encabezado del reporte y encaberzado de tabla
        if ($estatus == "Cerrado") {
            $titulo = "Lista De Problemas Cerrados En La Inspeccion Actual";
        }else{
            $titulo = "Lista De Todos Los Problemas Abiertos";
        }

        $datosEncabezado = [
            "tipoEncabezado" => 4,
            "titulo" => $titulo,
            "grupo" => "",
            "cliente" => $datosInspeccion[0]['nombreCliente'],
            "sitio" => $datosInspeccion[0]['nombreSitio'],
            "analistaTermografo" => $session->nombre,
            "nivelCertificacion" => $datosInpector[0]['nivelCertificacion'],
            "inspeccionAnterior" => $datosInspeccion[0]['No_Inspeccion_Ant'],
            "fecha_inspeccion_anterior" => $datosInspeccion[0]['fechaInspeccionAnterior'],
            "inspeccionActual" => $datosInspeccion[0]['No_Inspeccion'],
            "fecha_inspeccion_actual" => $datosInspeccion[0]['fechaInspeccionActual'],
            "anchoColumnas" => $w,
            "alineacionEncabezados" => $alineacion,
            "ruta_imagen_cliente" => $ruta_imagen_cliente
        ];

        $pdf = new PDF($datosEncabezado);
        $pdf->AddPage('L','A4',0);

        // Creando tabla
        $pdf->SetWidths($w);
        $pdf->SetAligns($alineacion);
        $colorearCelda = true;
        // Ubicando las celdas de la tabla
        $pdf->SetY(57);
        // Datos de la tabla
        foreach($problemas as $key => $value){

            $pdf->SetFont('Arial','',8);

            if ($colorearCelda) {
                $pdf->SetColorCell(array(236,236,236));
            }else{
                $pdf->SetColorCell(array(255,255,255));
            }

            $pdf->Row(array(
                "Equipo: ".$value['Ruta']."\nComentarios: ".$value['Component_Comment'],
                date("d/m/Y", strtotime($value["Fecha_Creacion"])),
                $value["numInspeccion"],
                $value["tipoInspeccion"]." ".$value["Numero_Problema"],
                $value["Estatus_Problema"],
                $value["Es_Cronico"],
                $value["Problem_Temperature"]!= "" ? $value["Problem_Temperature"]."°C" : " ",
                $value["Aumento_Temperatura"]!= "" ? $value["Aumento_Temperatura"]."°C" : " ",
                $value["StrSeveridad"],
            ));

            $colorearCelda = !$colorearCelda;

        }

        $pdf->Cell(array_sum($w),0,'','T');

        // Línea de cierre
        $currentTimeinSeconds = time();
        $nombrePdf = 'ETIC_LISTA_PROBLEMAS_'.strtoupper($estatus).'S_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        
        // if(count($problemas) > 0){
            $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);
        // }
        // $pdf->Output('I', $nombrePdf);

        return json_encode(200);
    }

    public function generarGraficaConcentradoProblemas(){
        $inspeccionesMdl = new InspeccionesMdl();
        $problemasMdl = new ProblemasMdl();
        $usuariosMdl = new UsuariosMdl();
        $session = session();

        // Consulta para los datos del analista termografo
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);

        //chart data
        $data=Array(
            "Total de Hallazgos"=>[
                'color'=>[74,127,194],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion
                        )
                    )
                )
            ],
            "Eléctricos Abiertos Críticos"=>[
                'color'=>[173,0,0],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                            // "problemas.Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                            "problemas.Estatus_Problema" => "Abierto",
                            "problemas.Id_Severidad" => "1D56EDB0-8D6E-11D3-9270-006008A19766"
                        )
                    )
                )
            ],
            'Eléctricos Abiertos Serios'=>[
                'color'=>[245,0,0],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                            // "problemas.Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                            "problemas.Estatus_Problema" => "Abierto",
                            "problemas.Id_Severidad" => "1D56EDB1-8D6E-11D3-9270-006008A19766"
                        )
                    )
                )
            ],
            'Eléctricos Abiertos Importantes'=>[
                'color'=>[245,100,0],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                            // "problemas.Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                            "problemas.Estatus_Problema" => "Abierto",
                            "problemas.Id_Severidad" => "1D56EDB2-8D6E-11D3-9270-006008A19766"
                        )
                    )
                )
            ],
            'Eléctricos Abiertos Menores'=>[
                'color'=>[225,208,0],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                            // "problemas.Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                            "problemas.Estatus_Problema" => "Abierto",
                            "problemas.Id_Severidad" => "1D56EDB3-8D6E-11D3-9270-006008A19766"
                        )
                    )
                )
            ],
            'Hallazgos Abiertos Mecánicos'=>[
                'color'=>[124,174,230],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Id_Tipo_Inspeccion" => "0D32B334-76C3-11D3-82BF-00104BC75DC2",
                            "problemas.Estatus_Problema" => "Abierto",
                        )
                    )
                )
            ],
            'Hallazgos Abiertos Visuales'=>[
                'color'=>[181,181,181],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Id_Tipo_Inspeccion" => "0D32B333-76C3-11D3-82BF-00104BC75DC2",
                            "problemas.Estatus_Problema" => "Abierto",
                        )
                    )
                )
            ],
            'Anomalías/Hallazgos Reparados'=>[
                'color'=>[0,149,2],
                'value'=> count(
                    $problemasMdl->getProblemas_SitioGrafica(
                        array(
                            "problemas.Id_Sitio" => $session->Id_Sitio,
                            "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                            "problemas.Estatus_Problema" => "Cerrado",
                        )
                    )
                )
            ],
        );

        // iMAGEN DEL CLIENTE
        // Obteniendo el nombre y ruta de la imagen del cliente
        $imagen_cliente = $datosInspeccion[0]['imagen_cliente'];
        $ruta_imagen_cliente = 'Archivos_ETIC/clientes_img/'.$imagen_cliente;
        // Datos para el encabezado del reporte y encaberzado de tabla
        $datosEncabezado = [
            "tipoEncabezado" => 5,
            "titulo" => "",
            "ruta_imagen_cliente" => $ruta_imagen_cliente
        ];

        $pdf = new PDF($datosEncabezado);
        $pdf->AddPage('L','A4',0);

        // Título
        $pdf->SetXY(86,10);
        $pdf->SetFont('Arial','B',13);
        $pdf->SetTextColor(0,22,102);
        $pdf->MultiCell(125,6,utf8_decode('Gráfica de anomalías/hallazgos detectadas durante la auditoría-inspección termográfica.'),0,'C');
        $pdf->SetTextColor(0,0,0);

        $pdf->SetY(27);
        // Apartado De datos de la inspeccion
        // if($pdf->datosInspeccion["grupo"] != ""){
        //     $pdf->SetFont('Arial','B',8); $pdf->Cell(80,4,utf8_decode($pdf->datosEncabezado["grupo"]),0,0,'L') ;$pdf->Ln();
        // }
        $pdf->SetFont('Arial','B',8); $pdf->Cell(100,4,utf8_decode($datosInspeccion[0]['nombreCliente']),0,0,'L'); $pdf->Ln();
        $pdf->SetFont('Arial','',8); $pdf->Cell(100,4,utf8_decode($datosInspeccion[0]['nombreSitio']),0,0,'L'); $pdf->Ln();
        $pdf->Cell(100,4,utf8_decode('Analista Termógrafo: '.$session->nombre),0,0,'L'); $pdf->Ln();
        $pdf->Cell(102,4,utf8_decode('Nivel De Certificación: '.$datosInpector[0]['nivelCertificacion']),0,0,'L'); $pdf->Ln();
        $pdf->SetXY(243, 27);
        $pdf->Cell(45,4, 'Fecha De Reporte: '.date("Y/m/d"),0,0,'R'); $pdf->Ln();
        // $pdf->SetX(242); $pdf->Cell(45,4,utf8_decode('No. Inspección Anterior: '.$datosInspeccion[0]['No_Inspeccion_Ant']),0,0,'R'); $pdf->Ln();
        // $pdf->SetX(242); $pdf->Cell(45,4,utf8_decode('No. Inspección Actual: '.$datosInspeccion[0]['No_Inspeccion']),0,0,'R');

        $pdf->SetX(218);
        $pdf->SetFont('Arial','',8); $pdf->Cell(34,4,utf8_decode('No. Inspección Anterior:'),0,0,'R');
        $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode($datosInspeccion[0]['No_Inspeccion_Ant']),0,0,'L');
        $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
        $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]["fechaInspeccionAnterior"]),0,0,'L');$pdf->Ln();
        $pdf->SetX(218);
        $pdf->SetFont('Arial','',8);$pdf->Cell(34,4,utf8_decode('No. Inspección Actual:'),0,0,'R');
        $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode($datosInspeccion[0]['No_Inspeccion']),0,0,'L');
        $pdf->SetFont('Arial','',8); $pdf->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
        $pdf->SetFont('Arial','',8); $pdf->Cell(15,4,utf8_decode($datosInspeccion[0]["fechaInspeccionActual"]),0,0,'L');$pdf->Ln();

        // Posicion de la grafica
        $chartX=10;
        $chartY=47;

        // Dimenciones
        $chartWidth=277;
        $chartHeight=145;

        // Margenes
        $chartTopPadding=10;
        $chartLeftPadding=10;
        $chartBottomPadding=15;
        $chartRightPadding=0;

        //chart box
        $chartBoxX=$chartX+$chartLeftPadding;
        $chartBoxY=$chartY+$chartTopPadding;
        $chartBoxWidth=$chartWidth-$chartLeftPadding-$chartRightPadding;
        $chartBoxHeight=$chartHeight-$chartBottomPadding-$chartTopPadding;

        //Ancho de la barra
        $barWidth=20;

        //$dataMax
        $dataMax=0;
        foreach($data as $item){
            if($item['value']>$dataMax)$dataMax=$item['value'];
        }

        //data paso
        $dataStep= 10;

        //set font, line width and color
        $pdf->SetFont('Arial','',9);
        $pdf->SetLineWidth(0.2);
        $pdf->SetDrawColor(0);

        //RECTANGULO DE BORDE
        // $pdf->Rect($chartX,$chartY,$chartWidth,$chartHeight);

        // EJE Y, LINEA VERTICAL
        $pdf->SetDrawColor(171,171,171);
        $pdf->Line(
            $chartBoxX ,
            $chartBoxY ,
            $chartBoxX ,
            ($chartBoxY+$chartBoxHeight)
            );
        // EJE X, LINEA HORIZONTAL
        $pdf->Line(
            $chartBoxX-2 ,
            ($chartBoxY+$chartBoxHeight) ,
            $chartBoxX+($chartBoxWidth) ,
            ($chartBoxY+$chartBoxHeight)
            );

        ///vertical axis
        //calculate chart's y axis scale unit
        // Para que no marque error en divicion entre cero, si vale cero el dataMax le asignamos el valor de 1
        if ($dataMax == 0 || $dataMax == "") {
            $dataMax = 1;
        }

        $yAxisUnits=$chartBoxHeight/$dataMax;

        //draw the vertical (y) axis labels
        for($i=0 ; $i<=$dataMax ; $i+=$dataStep){
            //y position
            $yAxisPos=$chartBoxY+($yAxisUnits*$i);
            //draw y axis line
            $pdf->SetLineWidth(0.1);
            $pdf->SetDrawColor(171,171,171);
            $pdf->SetDash(3,3);
            $pdf->Line(
                $chartBoxX-2 ,
                $yAxisPos ,
                // $chartBoxX ,
                282,
                $yAxisPos
            );
            //set cell position for y axis labels
            $pdf->SetXY($chartBoxX-$chartLeftPadding , $yAxisPos-2);
            //$pdf->Cell($chartLeftPadding-4 , 5 , $dataMax-$i , 1);---------------
            $pdf->Cell($chartLeftPadding-4 , 5 , $dataMax-$i, 0 , 0 , 'R');
        }
        $pdf->SetLineWidth(0.2);
        $pdf->SetDrawColor(0);
        $pdf->SetDash();

        ///horizontal axis
        //set cells position
        $pdf->SetXY($chartBoxX , $chartBoxY+$chartBoxHeight);

        //cell's width
        $xLabelWidth=$chartBoxWidth / count($data);

        //loop horizontal axis and draw the barra
        $barXPos=0;
        foreach($data as $itemName=>$item){
            //print the labelpara cuando no es multicell
            //$pdf->Cell($xLabelWidth , 5 , $itemName , 1 , 0 , 'C');--------------

            ///drawing the barra
            //barra color
            $pdf->SetDrawColor(171,171,171);
            $pdf->SetFillColor($item['color'][0],$item['color'][1],$item['color'][2]);
            //barra height
            $barHeight=$yAxisUnits*$item['value'];
            //barra x position
            $barX=($xLabelWidth/2)+($xLabelWidth*$barXPos);
            $barX=$barX-($barWidth/2);
            $barX=$barX+$chartBoxX;
            //barra y position
            $barY=$chartBoxHeight-$barHeight;
            $barY=$barY+$chartBoxY;

            $pdf->SetXY($barX , $barY-4);
            $pdf->SetTextColor(0,22,102);
            $pdf->SetFont('Arial','B',12);
            $pdf->Cell($barWidth,4,$item['value'],0,0,'C');
            $pdf->SetFont('Arial','',9);
            $pdf->SetTextColor(0,0,);
            //draw the barra
            $pdf->Rect($barX,$barY,$barWidth,$barHeight,'DF');
            // Colocamos las etiquetas de cada barra
            $pdf->SetXY($barX-6, $chartBoxY+$chartBoxHeight);
            $pdf->MultiCell($xLabelWidth,5,utf8_decode($itemName),0,"C");

            //Siguiente serie
            $barXPos++;
        }

        // Cuantos cronicos
        $pdf->SetXY(221, 47);
        $pdf->SetFillColor(232,76,76);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFont('Arial','B',10);
        $pdf->Cell(66,8,utf8_decode('Anomalías / Hallazgos Crónicos: '.count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Es_Cronico" => "SI",
                )
            )
        )),0,0,'C',true);
        $pdf->SetTextColor(0,0,0);

        //axis labels
        // $pdf->SetFont('Arial','B',12);
        // $pdf->SetXY($chartX,$chartY);
        // $pdf->Cell(100,10,"Leyenda parte izquierda",0);
        // $pdf->SetXY(($chartWidth/2)-50+$chartX,$chartY+$chartHeight-($chartBottomPadding/2));
        // $pdf->Cell(100,5,"leyenda para la parte de abajo",0,0,'C');


        // Línea de cierre
        $currentTimeinSeconds = time();
        $nombrePdf = 'ETIC_GRAFICA_ANOMALIAS_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);
        // $pdf->Output('I', $nombrePdf);

        return json_encode(200);
    }

    public function generarResultadoDeAnalisis(){
        // $this->generarReporteBaseLine();
        // Generar reportes previos
        $this->generarGraficaConcentradoProblemas();
        $this->generarReporteListaProblemas("Abierto");
        $this->generarReporteListaProblemas("Cerrado");
        $this->generarReporteListaProblemasExcel($this->request->getPost('fecha_inicio_ra'), $this->request->getPost('fecha_fin_ra'));

        // Apartado para la creacion de la portada de todo el reporte
        $inspeccionesMdl = new InspeccionesMdl();
        $problemasMdl = new ProblemasMdl();
        $usuariosMdl = new UsuariosMdl();
        $sitiosMdl = new SitiosMdl();
        $session = session();
        // Formato de fecha en español
        date_default_timezone_set("America/Mexico_City");
        setlocale(LC_TIME,"es_MX.UTF-8",'esp');

        // Consulta para los datos del analista termografo
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);
        // CONSULTA DEL SITIO
        $datosSitio = $sitiosMdl->obtenerRegistros($session->Id_Sitio);

        // Obteniendo el nombre y ruta de la imagen del cliente
        $img_portada = $this->request->getPost('nombre_img_portada');

        $ruta_img_portada = 'Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Imagenes/'.$img_portada;

        $cliente = $datosInspeccion[0]['nombreCliente'];
        $sitio = $datosSitio[0]['Sitio'];
        $Direccion = $datosSitio[0]['Direccion'] != "" ? $datosSitio[0]['Direccion'].", " : "";
        $Colonia = $datosSitio[0]['Colonia'] != "" ? $datosSitio[0]['Colonia'].", " : "";
        $Municipio = $datosSitio[0]['Municipio'] != "" ? $datosSitio[0]['Municipio'] : "";
        $Estado = $datosSitio[0]['Estado'] != "" ? ", ".$datosSitio[0]['Estado'] : "";
        $nombreGrupoSitio = $datosSitio[0]['nombreGrupoSitio'] != "" ? $datosSitio[0]['nombreGrupoSitio'] : "";
        $direccion_completa = $Direccion.$Colonia.$Municipio.$Estado;

        $nombre_contactos = $this->request->getPost('nombre_contacto');
        $puesto_contactos = $this->request->getPost('puesto_contacto');

        /* Convertimos la fecha a marca de tiempo */
        $fecha_inicio = $this->request->getPost('fecha_inicio_ra');
        $fecha_fin = $this->request->getPost('fecha_fin_ra');

        if($fecha_inicio == $fecha_fin){
            $fecha_inicio_format = strftime('%e de %B del %Y', strtotime($fecha_inicio));
            
            $strFecha = $fecha_inicio_format;
        }else{
            $fecha_inicio_format = strftime('%e de %B', strtotime($fecha_inicio));
            $fecha_fin_format = strftime('%e de %B del %Y', strtotime($fecha_fin));

            $strFecha = "Del ".$fecha_inicio_format." al ".$fecha_fin_format;
        }

        $fecha_inspeccion_anterior = strftime('%e de %B del %Y', strtotime($datosInspeccion[0]["fechaInspeccionAnterior_reporte_resultado_analisis"]));
        $analista_termorafo = $session->nombre;
        $nivel_certificacion = $datosInpector[0]['nivelCertificacion'];
        $descripcion_reporte = $this->request->getPost('descripcion_reporte');
        $recomendacion_reporte = $this->request->getPost('recomendacion_reporte');
        $imagen_recomendacion = $this->request->getPost('imagen_recomendacion');
        $imagen_recomendacion_2 = $this->request->getPost('imagen_recomendacion_2');
        $referencia_reporte = $this->request->getPost('referencia_reporte');
        $elementos_inspeccionados = $this->request->getPost('areas_inspeccionadas');

        $total_hallazgos = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion
                )
            )
        );
    
        $total_electricos = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );
    
        $cronicos_electricos = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Es_Cronico" => "SI",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto",
                )
            )
        );
    
        $electricos_cerrados = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    "problemas.Estatus_Problema" => "Cerrado"
                )
            )
        );
    
        $noCronicos_electricos = $total_electricos - $cronicos_electricos;
    
        $total_visuales = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Tipo_Inspeccion" => "0D32B333-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );
    
        $cronicos_visuales = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Es_Cronico" => "SI",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B333-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto",
                )
            )
        );
    
        $visuales_cerrados = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Tipo_Inspeccion" => "0D32B333-76C3-11D3-82BF-00104BC75DC2",
                    "problemas.Estatus_Problema" => "Cerrado"
                )
            )
        );
    
        $noCronicos_visuales = $total_visuales - $cronicos_visuales;
    
        $total_mecanicos = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Tipo_Inspeccion" => "0D32B334-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );
    
        $cronicos_mecanicos = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Es_Cronico" => "SI",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B334-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto",
                )
            )
        );
    
        $mecanicos_cerrados = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Tipo_Inspeccion" => "0D32B334-76C3-11D3-82BF-00104BC75DC2",
                    "problemas.Estatus_Problema" => "Cerrado"
                )
            )
        );
    
        $noCronicos_mecanicos = $total_mecanicos - $cronicos_mecanicos;
    
        $total_t1 = $total_electricos + $total_mecanicos;
        $total_cronicos_t1 = $cronicos_electricos + $cronicos_mecanicos;
        $total_cerrados_t1 = $electricos_cerrados + $mecanicos_cerrados;

        $total_criticos = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Severidad" => "1D56EDB0-8D6E-11D3-9270-006008A19766",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );

        $total_serios = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Severidad" => "1D56EDB1-8D6E-11D3-9270-006008A19766",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );

        $total_importantes = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Severidad" => "1D56EDB2-8D6E-11D3-9270-006008A19766",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );

        $total_menores = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Severidad" => "1D56EDB3-8D6E-11D3-9270-006008A19766",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "problemas.Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );

        $total_normal = count(
            $problemasMdl->getProblemas_SitioGrafica(
                array(
                    "problemas.Id_Sitio" => $session->Id_Sitio,
                    "problemas.Id_Inspeccion" => $session->Id_Inspeccion,
                    "problemas.Id_Severidad" => "1D56EDB4-8D6E-11D3-9270-006008A19766",
                    "problemas.Id_Tipo_Inspeccion" => "0D32B331-76C3-11D3-82BF-00104BC75DC2",
                    // "Id_Tipo_Inspeccion" => "0D32B332-76C3-11D3-82BF-00104BC75DC2",
                    // "Estatus_Problema" => "Abierto"
                )
            )
        );

        #Creamos el objeto pdf (con medidas en milímetros):
        $pdf = new PDF_RA('P', 'mm', array(220,280));
        
        #Establecemos los márgenes izquierda, arriba y derecha:
        $pdf->SetMargins(25, 25 , 25);

        #Establecemos el margen inferior:
        $pdf->SetAutoPageBreak(true,41);
        
        // PAGINA 1 PORTADA
        $pdf->AddPage();

        // imagen del ETIC
        $pdf->Image(base_url('/img/sistema/ETIC_logo.jpg'),25,25,53);
        // Imagen ISO
        // $pdf->Image(base_url('/img/sistema/iso_img.png'),99,22,20);
        $pdf->Image(base_url('/img/sistema/iso_img.png'),18,9,20);
        
        // iMAGEN DEL CLIENTE
        // Obteniendo el nombre y ruta de la imagen del cliente
        $imagen_cliente = $datosInspeccion[0]['imagen_cliente'];
        $ruta_imagen_cliente = 'Archivos_ETIC/clientes_img/'.$imagen_cliente;
        // Imprimiendo la imagen del cliente
        // $pdf->Image(base_url($ruta_imagen_cliente),142,23,53,24);
        if(is_dir(ROOTPATH."public/".$ruta_imagen_cliente) != true && file_exists(ROOTPATH."public/".$ruta_imagen_cliente)){
            $pdf->Image(base_url($ruta_imagen_cliente),139,23,51,21);
        }
        
        // Titulo
        $pdf->setY(55);
        $pdf->SetFont('Arial','',20);
        $pdf->MultiCell(0,9,utf8_decode("F-PRS-02 - Resultados de Análisis de Riesgos con Termografía Infrarroja"),0,'C');

        // iMAGEN PORTADA
        if(is_dir(ROOTPATH."public/".$ruta_img_portada) != true && file_exists(ROOTPATH."public/".$ruta_img_portada)){
            $pdf->Image(base_url($ruta_img_portada),57,80,106);
        }

        $pdf->setY(170);
        $pdf->SetWidths(array(47,0));
        // Nombre cliente
        $pdf->SetFont('Arial','',11);
        $pdf->Row(array("Cliente:",$cliente));
        // Grupo de sitios
        if ($nombreGrupoSitio != "") {
            $py_detalle_ubi = $pdf->GetY();
            $pdf->MultiCell(47,5,"",0,'L');
            $pdf->SetXY(72,$py_detalle_ubi);
            $pdf->MultiCell(0,5,$nombreGrupoSitio,0,'L');
        }
        // Nombre sitio
        $pdf->Row(array("",$sitio));
        // Direccion sitio
        $pdf->Row(array("",strtoupper($direccion_completa)));
        $pdf->Ln(13);

        // APARTADO DE CONTACTOS
        for ($i=0; $i <4 ; $i++) { 
            if($nombre_contactos[$i] != "" && $puesto_contactos[$i] != ""){
                $txt = "";
                if($i == 0){
                    $txt = "Contactos:";
                }

                $pdf->SetFont('Arial','B',11);
                $pdf->Cell(46,5,$txt,0,0,'L');
                $pdf->SetFont('Arial','',11);
                $pdf->Cell($pdf->GetStringWidth($nombre_contactos[$i])+2,5,utf8_decode($nombre_contactos[$i]." -"),0,0,'L');
                $pdf->SetFont('Arial','B',11);
                $pdf->Cell(0,5,utf8_decode(" ".$puesto_contactos[$i]),0,0,'L');
                $pdf->Ln();
            }
        }
        $pdf->Ln(8);

        $pdf->Row(array("Fecha Servicio:",$strFecha));
        $pdf->Row(array("Fecha Servicio anterior:",$fecha_inspeccion_anterior));
        $pdf->Row(array("Realizó:",$analista_termorafo." / Termógrafo Certificado ".$nivel_certificacion."."));

        // agregamos lapagina
        $pdf->AddPage();

        $pdf->SetY(35);
        $pdf->SetFont('Arial','B',11);
        $pdf->Cell(0,5,utf8_decode($cliente),0,0,'R'); $pdf->Ln();
        // $pdf->Cell(0,5,$nombreGrupoSitio,1,0,'R');
        $pdf->MultiCell(0,5,utf8_decode($nombreGrupoSitio),0,'R'); $pdf->Ln();
        
        $pdf->Ln(20);
        
        $pdf->SetFont('Arial','',11);
        $pdf->SetFont('Arial','U');
        $pdf->Cell(0,5,utf8_decode("Asunto: Entrega de informe servicio análisis de riesgo con termografía infrarroja."),0,0,'R');
        $pdf->SetFont('Arial','',11);
        $pdf->Ln(20);

        $pdf->SetFont('Arial','B',11);
        if ($nombre_contactos[1] != "") {
            $pdf->Cell(0,5,utf8_decode("Estimados:"),0,0,'L');
        }else{
            $pdf->Cell(0,5,utf8_decode("Estimado:"),0,0,'L');
        }
        $pdf->SetFont('Arial','',11);
        $pdf->Ln();
        
        // APARTADO DE CONTACTOS
        for ($i=0; $i <4 ; $i++) { 
            if($nombre_contactos[$i] != "" && $puesto_contactos[$i] != ""){
                $pdf->SetFont('Arial','',11);
                $pdf->Cell($pdf->GetStringWidth($nombre_contactos[$i])+2,5,utf8_decode($nombre_contactos[$i]),0,0,'L');
                $pdf->Ln();
            }
        }
        // $pdf->Ln(20);
        $pdf->SetY(120);

        if($fecha_inicio == $fecha_fin){
            $fecha_inicio_format = strftime('%e de %B del %Y', strtotime($fecha_inicio));
            
            $strFecha = "el ".$fecha_inicio_format;
        }else{
            $fecha_inicio_format = strftime('%e de %B', strtotime($fecha_inicio));
            $fecha_fin_format = strftime('%e de %B del %Y', strtotime($fecha_fin));

            $strFecha = "del ".$fecha_inicio_format." al ".$fecha_fin_format;
        }
        $pdf->MultiCell(0,5,utf8_decode("Por este medio, hacemos entrega de los resultados finales de la inspección por termografía infrarroja realizada en las instalaciones eléctricas y mecánicas de ".$sitio.", ubicadas en ".$Municipio." ".$Estado.". Servicio realizado ".$strFecha."."),0,"J");
        $pdf->Ln(5);
        $pdf->MultiCell(0,5,utf8_decode("Agradecemos a ustedes la confianza y facilidades otorgadas durante la ejecución de nuestro servicio. Así mismo, expresamos nuestro reconocimiento a su personal técnico por su colaboración y profesionalismo."),0,"J");
        $pdf->Ln(5);
        $pdf->MultiCell(0,5,utf8_decode("Sin más, quedamos atentos a sus amables comentarios"),0,"J");
        $pdf->Ln(5);
        $pdf->MultiCell(0,5,utf8_decode("Cordialmente,"),0,"J");
        $pdf->Ln(30);

        $pdf->Cell(0,5,utf8_decode($analista_termorafo),0,0); $pdf->Ln();
        $pdf->Cell(0,5,utf8_decode("Especialistas en Termografía Industrial y"),0,0); $pdf->Ln();
        $pdf->Cell(0,5,utf8_decode("Corporativa, S.A. de C.V."),0,0); $pdf->Ln();
        $pdf->Cell(0,5,utf8_decode("www.etic-infrared.mx"),0,0); $pdf->Ln();
        $pdf->Cell(15,5,utf8_decode("Celular:"),0,0);
        $pdf->Cell(0,5,utf8_decode($datosInpector[0]['Telefono']),0,0); $pdf->Ln();
        $pdf->SetFont('Arial','U');
        $pdf->SetTextColor(0,4,173);
        $pdf->Cell(0,5,utf8_decode($datosInpector[0]['Email']),0,0);
        $pdf->SetTextColor(0,0,0);

        // agregamos lapagina
        $pdf->AddPage();

        $pdf->SetY(35);
        $pdf->SetTextColor(0,2,83);
        $pdf->SetFont('Arial','BU',11);
        $pdf->Cell(0,5,utf8_decode("I. Resumen Ejecutivo"),0,0,'L'); $pdf->Ln();
        $pdf->SetTextColor(0,0,0);
        $pdf->Ln(8);
        
        $pdf->SetFont('Arial','',11);
        $pdf->MultiCell(0,5,utf8_decode("Personal de ETIC, S.A. de C.V., se presentó en las instalaciones de ".$sitio." ubicadas en ".$Municipio.", ".$Estado.", ".$strFecha.", con el objeto de realizar la  Inspección por Termografía Infrarroja en los equipos seleccionados para su estudio definidos en conjunto con responsables de cada área, descargando la información recabada durante este servicio en el software ETIC System©."),0,"J");
        $pdf->Ln(15);

        $pdf->SetTextColor(0,4,173);
        $pdf->SetFont('Arial','B',12);
        $pdf->MultiCell(0,5,utf8_decode("1. Descripción del reporte:"),0,"J");
        $pdf->SetTextColor(0,0,0);
        $pdf->Ln(10);

        $pdf->SetFont('Arial','',11);
        $pdf->Cell(7,5,utf8_decode("a."),0,0,"L");
        $pdf->cellMultiColor([
            [
                'text' => 'Se generó el inventario de todos los equipos críticos en nuestra base de datos ETIC System, ',
                'color' => [0, 0, 0],
            ],
            [
                'text' => '(Inventario De Equipo). ',
                'color' => [79, 129, 189],
            ],
            [
                'text' => 'Se colocó un código de barras que ayudará a tener un control de rápida identificación para futuras inspecciones.',
                'color' => [0, 0, 0],
            ],
        ]);
        // El salto es de 5 pero aqui hay que duplicarlo por la funcion multicolor
        $pdf->Ln(10);

        // Escribiendo con html
        // $pdf->Cell(7,5,utf8_decode("a."),1,0,"L");
        // $pdf->WriteHTML(utf8_decode('hóla <b>mundo<b>'));

        $pdf->Cell(7,5,utf8_decode("b."),0,0,"L");
        $pdf->cellMultiColor([
            [
                'text' => 'Se tomaron termogramas como referencia del comportamiento actual de los equipos esenciales para la operación definidos en la reunión inicial, se documentan en la sección de  baseline ',
                'color' => [0, 0, 0],
            ],
            [
                'text' => '(Baseline Equipo En Monitoreo Informe de Tendencias) ',
                'color' => [79, 129, 189],
            ],
            [
                'text' => 'con objeto de conocer la tendencia del comportamiento de temperatura de estos equipos.',
                'color' => [0, 0, 0],
            ],
        ]);
        // El salto es de 5 pero aqui hay que duplicarlo por la funcion multicolor
        $pdf->Ln(10);


        $pdf->Cell(7,5,utf8_decode("c."),0,0,"L");
        $pdf->cellMultiColor([
            [
                'text' => 'Los problemas identificados en esta inspección, han sido documentados en las secciones eléctrica, mecánica y anomalías visuales; según aplique, ',
                'color' => [0, 0, 0],
            ],
            [
                'text' => ' (Eléctrico, Mecanico, Visual). ',
                'color' => [79, 129, 189],
            ],
        ]);
        // El salto es de 5 pero aqui hay que duplicarlo por la funcion multicolor
        $pdf->Ln(10);

        $pdf->Cell(7,5,utf8_decode("e."),0,0,"L");
        $pdf->MultiCell(0,5,utf8_decode("El total de problemas y anomalías documentados se enlistan en la última sección agrupándolos de acuerdo al siguiente criterio:"),0,"J");
        $pdf->Ln(5);

        $pdf->SetX(37);
        $pdf->Cell(3,5,chr(149),0,0,"L");
        $pdf->MultiCell(0,6,utf8_decode("Lista de todos los problemas abiertos."),0,"J");
        $pdf->Ln(-1);
        
        $pdf->SetX(37);
        $pdf->Cell(3,5,chr(149),0,0,"L");
        $pdf->MultiCell(0,6,utf8_decode("Lista de todos los problemas cerrados."),0,"J");
        $pdf->Ln(-1);

        // Apartado de descripciones automaticas
        foreach ($descripcion_reporte as $descripcion) {
            if($descripcion != ""){
                $pdf->SetX(37);
                $pdf->Cell(3,5,chr(149),0,0,"L");
                $pdf->MultiCell(0,6,utf8_decode($descripcion),0,"J");
                $pdf->Ln(-1);
            }
        }

        // agregamos lapagina
        $pdf->AddPage();

        $pdf->SetY(35);
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFont('Arial','',11);
        $pdf->MultiCell(0,5,utf8_decode("Durante esta inspección, se tuvo como objeto principal, la revisión de las instalaciones eléctricas críticas en la continuidad de su operación para identificar anomalías térmicas y/o anomalías visuales en los componentes y/o conexiones"),0,"J");
        $pdf->Ln(5);

        $pdf->SetTextColor(0,4,173);
        $pdf->SetFont('Arial','B',11);
        $pdf->SetX(37);
        $pdf->MultiCell(0,5,utf8_decode("Las áreas/equipos inspeccionados durante el estudio de termografía infrarroja son las siguientes:"),0,"J");
        $pdf->SetTextColor(0,0,0);
        $pdf->Ln(5);

        // Apartado de areas inspeccionadas
        $pdf->SetFont("");
        foreach ($elementos_inspeccionados as $elemento) {
            $pdf->SetX(37);
            $pdf->Cell(3,5,chr(149),0,0,"L");
            $pdf->MultiCell(0,5,utf8_decode($elemento),0,"J");
            $pdf->Ln(2);
        }
        $pdf->Ln(8);

        if(($pdf->GetY()+ 28 +9) > 241){
            $pdf->AddPage();
            $pdf->SetY(35);
        }
        // POSICION DE X DE LAS COLUMNAS DE LA PRIMER TABLA
        $px_c1 = 38;
        $px_c2 = 75;
        $px_c3 = 117;
        $px_c4 = 147;

        $py_f1 = $pdf->GetY();
        $py_f2 = $py_f1 + 10;
        $py_f3 = $py_f2 + 6;
        $py_f4 = $py_f3 + 6;
        $py_f5 = $py_f4 + 6;

        $pdf->SetXY($px_c1,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(37,10,utf8_decode("Tipo de Anomalía"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(37,6,utf8_decode("Eléctricos"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(37,6,utf8_decode("Mecánico"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,32,96);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(37,6,utf8_decode("Total:"),1,"R",true);

        $pdf->SetFont("");
        $pdf->SetXY($px_c2,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(37,5,utf8_decode("Anomalías\nDocumentadas"),1,"C",true);
        $pdf->SetXY($px_c2,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(37,6,$total_electricos,1,"C",true);
        $pdf->SetXY($px_c2,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(37,6,$total_mecanicos,1,"C",true);
        $pdf->SetXY($px_c2,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,32,96);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(37,6,$total_t1,1,"C",true);
        
        $pdf->SetFont("");
        $pdf->SetXY($px_c3,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(30,5,utf8_decode("Anomalías\nCrónicas"),1,"C",true);
        $pdf->SetXY($px_c3,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(30,6,$cronicos_electricos,1,"C",true);
        $pdf->SetXY($px_c3,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(30,6,$cronicos_mecanicos,1,"C",true);
        $pdf->SetXY($px_c3,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,32,96);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(30,6,$total_cronicos_t1,1,"C",true);
        
        $pdf->SetFont("");
        $pdf->SetXY($px_c4,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(25,5,utf8_decode("Cerradas\nEn Sitio"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(25,6,$electricos_cerrados,1,"C",true);
        $pdf->SetXY($px_c4,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(25,6,$mecanicos_cerrados,1,"C",true);
        $pdf->SetXY($px_c4,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,32,96);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(25,6,$total_cerrados_t1,1,"C",true);
        $pdf->Ln(4);
        
        $pdf->SetFont("");
        $pdf->SetTextColor(0,32,96);
        $pdf->Cell(0,5,utf8_decode("Tabla No. 1 Listado de anomalías térmicas documentados en esta inspección."),0,0,"C"); $pdf->Ln();
        $pdf->Ln(8);

        
        if(($pdf->GetY()+ 22 + 9) > 241){
            $pdf->AddPage();
            $pdf->SetY(35);
        }
        // POSICION DE X DE LAS COLUMNAS DE LA SEGUNDA TABLA
        $px_c1 = 38;
        $px_c2 = 75;
        $px_c3 = 117;
        $px_c4 = 147;

        $py_f1 = $pdf->GetY();
        $py_f2 = $py_f1 + 10;
        $py_f3 = $py_f2 + 6;

        $pdf->SetXY($px_c1,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(37,10,utf8_decode("Tipo de Anomalía"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(37,6,utf8_decode("Visuales"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f3);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,32,96);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(37,6,utf8_decode("Total:"),1,"R",true);

        $pdf->SetFont("");
        $pdf->SetXY($px_c2,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(37,5,utf8_decode("Anomalías\nDocumentados"),1,"C",true);
        $pdf->SetXY($px_c2,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(37,6,$total_visuales,1,"C",true);
        $pdf->SetXY($px_c2,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(37,6,$total_visuales,1,"C",true);
        
        $pdf->SetFont("");
        $pdf->SetXY($px_c3,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(30,5,utf8_decode("Anomalías\nCrónicos"),1,"C",true);
        $pdf->SetXY($px_c3,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(30,6,$cronicos_visuales,1,"C",true);
        $pdf->SetXY($px_c3,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,0,0);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(30,6,$cronicos_visuales,1,"C",true);
        
        $pdf->SetFont("");
        $pdf->SetXY($px_c4,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(25,5,utf8_decode("Cerrados\nEn Sitio"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(25,6,$visuales_cerrados,1,"C",true);
        $pdf->SetXY($px_c4,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(0,32,96);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(25,6,$visuales_cerrados,1,"C",true);
        $pdf->Ln(4);
        
        $pdf->SetFont("");
        $pdf->SetTextColor(0,32,96);
        $pdf->Cell(0,5,utf8_decode("Tabla No. 2 Listado de anomalías documentadas en esta inspección"),0,0,"C"); $pdf->Ln();
        $pdf->Ln(8);
        
        // POSICION DE X DE LAS COLUMNAS DE LA TERCER TABLA
        if(($pdf->GetY() + 64 +24) > 241){
            $pdf->AddPage();
            $pdf->SetY(35);
        }
        $px_c1 = 25;
        $px_c2 = $px_c1 + 35;
        $px_c3 = $px_c2 + 35;
        $px_c4 = $px_c3 + 32;

        $py_f1 = $pdf->GetY();
        $py_f2 = $py_f1 + 10;
        $py_f3 = $py_f2 + 12;
        $py_f4 = $py_f3 + 12;
        $py_f5 = $py_f4 + 18;
        $py_f6 = $py_f5 + 12;

        $pdf->SetXY($px_c1,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(70,5,utf8_decode("Clasificación por diferencial\nde temperatura"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(35,12,utf8_decode("Críticos"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f3);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(227,108,10);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(35,12,utf8_decode("Serios"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,192,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(35,18,utf8_decode("Importantes"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f5);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(35,12,utf8_decode("Menores"),1,"C",true);
        $pdf->SetXY($px_c1,$py_f6);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(35,12,utf8_decode("Normal"),1,"C",true);

        $pdf->SetXY($px_c2,$py_f2);
        $pdf->SetFont("","");
        $pdf->SetTextColor(74,69,69);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(35,12,utf8_decode("Mayores a 16°C"),1,"C",true);
        $pdf->SetXY($px_c2,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(74,69,69);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(35,12,utf8_decode("De 9°C a 15°C"),1,"C",true);
        $pdf->SetXY($px_c2,$py_f4);
        $pdf->SetFont("","");
        $pdf->SetTextColor(74,69,69);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(35,18,utf8_decode("De 4°C a 8°C"),1,"C",true);
        $pdf->SetXY($px_c2,$py_f5);
        $pdf->SetFont("","");
        $pdf->SetTextColor(74,69,69);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(35,12,utf8_decode("De 1°C a 3°C"),1,"C",true);
        $pdf->SetXY($px_c2,$py_f6);
        $pdf->SetFont("","");
        $pdf->SetTextColor(74,69,69);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(35,12,utf8_decode("0°"),1,"C",true);
        
        $pdf->SetFont("");
        $pdf->SetXY($px_c3,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(32,5,utf8_decode("Anomalías\nDocumentadas"),1,"C",true);
        $pdf->SetXY($px_c3,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(32,12,$total_criticos,1,"C",true);
        $pdf->SetXY($px_c3,$py_f3);
        $pdf->SetFont("");
        $pdf->SetTextColor(227,108,10);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(32,12,$total_serios,1,"C",true);
        $pdf->SetXY($px_c3,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,192,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(32,18,$total_importantes,1,"C",true);
        $pdf->SetXY($px_c3,$py_f5);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(32,12,$total_menores,1,"C",true);
        $pdf->SetXY($px_c3,$py_f6);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(32,12,$total_normal,1,"C",true);
        
        $pdf->SetFont("");
        $pdf->SetXY($px_c4,$py_f1);
        $pdf->SetTextColor(255,255,255);
        $pdf->SetFillColor(51,121,204);
        $pdf->MultiCell(58,10,utf8_decode("Acción Recomendada"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f2);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,0,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(58,6,utf8_decode("Discrepancia mayor, reparar inmediatamente"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f3);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(227,108,10);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(58,6,utf8_decode("Monitoreo hasta que se pueda realizar medidas correctivas"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f4);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(255,192,0);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(58,6,utf8_decode("Indica posible deficiencia, reparar según lo permita el tiempo"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f5);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(255,255,255);
        $pdf->MultiCell(58,6,utf8_decode("Posible deficiencia, requiere de investigación"),1,"C",true);
        $pdf->SetXY($px_c4,$py_f6);
        $pdf->SetFont("","B");
        $pdf->SetTextColor(0,112,192);
        $pdf->SetFillColor(236,236,236);
        $pdf->MultiCell(58,12,utf8_decode("---"),1,"C",true);
        $pdf->Ln(4);

        $pdf->SetFont('Arial','',11);
        $pdf->SetTextColor(0,32,96);
        $pdf->MultiCell(0,5,utf8_decode('Tabla No. 3 Listado de anomalías térmicas eléctricas por diferencial de temperatura Agrupados de acuerdo al Diferencial de Temperatura, basado en la comparación entre componentes similares bajo similar carga (ANSI-NETA) "International Electric Testing Association Inc-Approved American National Standards"'),0,"C");
        $pdf->SetTextColor(0,0,0);

        // agregamos lapagina
        $pdf->AddPage();
        $pdf->SetY(35);
        $pdf->SetTextColor(0,4,173);
        $pdf->SetFont('Arial','B',12);
        $pdf->MultiCell(0,5,utf8_decode("2. Recomendaciones:"),0,"J");
        $pdf->SetTextColor(0,0,0);
        $pdf->Ln(10);

        $pdf->SetFont('Arial','',11);

        $num_iteracion= 0;
        foreach ($recomendacion_reporte as $recomendacion) {

            // POSICION DE X DE LAS COLUMNAS DE LA TERCER TABLA
            if(($pdf->GetY() + 65) > 241){
                $pdf->AddPage();
                $pdf->SetY(35);
            }

            $pdf->Cell(3,5,chr(149),0,0,"L");
            $pdf->MultiCell(0,5,utf8_decode($recomendacion),0,"J");
            $salto_line = 1;

            if ($imagen_recomendacion[$num_iteracion] != "" && $imagen_recomendacion_2[$num_iteracion] != "") {

                // Obteniendo el nombre y ruta de la imagen del cliente
                $img_recomendacion = $imagen_recomendacion[$num_iteracion];
                $img_recomendacion_2 = $imagen_recomendacion_2[$num_iteracion];
                $ruta_img_recomendacion = 'Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Imagenes/'.$img_recomendacion;
                $ruta_img_recomendacion_2 = 'Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Imagenes/'.$img_recomendacion_2;

                if(is_dir(ROOTPATH."public/".$ruta_img_recomendacion) != true && file_exists(ROOTPATH."public/".$ruta_img_recomendacion)){
                    $pdf->Image(base_url($ruta_img_recomendacion),28,$pdf->GetY()+1,60,50);
                }
                if(is_dir(ROOTPATH."public/".$ruta_img_recomendacion_2) != true && file_exists(ROOTPATH."public/".$ruta_img_recomendacion_2)){
                    $pdf->Image(base_url($ruta_img_recomendacion_2),125,$pdf->GetY()+1,60,50);
                    $salto_line = 53;
                }
                
            }elseif ($imagen_recomendacion[$num_iteracion] != "" && $imagen_recomendacion_2[$num_iteracion] == "") {
                // Obteniendo el nombre y ruta de la imagen del cliente
                $img_recomendacion = $imagen_recomendacion[$num_iteracion];
                $ruta_img_recomendacion = 'Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Imagenes/'.$img_recomendacion;
                
                if(is_dir(ROOTPATH."public/".$ruta_img_recomendacion) != true && file_exists(ROOTPATH."public/".$ruta_img_recomendacion)){
                    $pdf->Image(base_url($ruta_img_recomendacion),75,$pdf->GetY()+1,60,50);
                    $salto_line = 53;
                }
            }elseif ($imagen_recomendacion[$num_iteracion] == "" && $imagen_recomendacion_2[$num_iteracion] != "") {
                // Obteniendo el nombre y ruta de la imagen del cliente
                $img_recomendacion_2 = $imagen_recomendacion_2[$num_iteracion];
                $ruta_img_recomendacion_2 = 'Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Imagenes/'.$img_recomendacion_2;
                
                if(is_dir(ROOTPATH."public/".$ruta_img_recomendacion_2) != true && file_exists(ROOTPATH."public/".$ruta_img_recomendacion_2)){
                    $pdf->Image(base_url($ruta_img_recomendacion_2),75,$pdf->GetY()+1,60,50);
                    $salto_line = 53;
                }
            }
            
            $pdf->Ln($salto_line);
            $num_iteracion ++;
        }

        // agregamos lapagina
        $pdf->AddPage();
        $pdf->SetY(35);
        $pdf->SetTextColor(0,4,173);
        $pdf->SetFont('Arial','B',12);
        $pdf->MultiCell(0,5,utf8_decode("3. Referencias:"),0,"J");
        $pdf->SetTextColor(0,0,0);
        $pdf->Ln(10);

        // Imprimiendo refrencias
        $pdf->SetFont('Arial','',11);
        foreach ($referencia_reporte as $referencia) {
            $pdf->Cell(3,5,chr(149),0,0,"L"); $pdf->MultiCell(0,5,utf8_decode($referencia),0,"J");
        }

        // Línea de cierre
        $currentTimeinSeconds = time();
        $nombrePdf = 'ETIC_PORTADA_REPORTE_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);
        // $pdf->Output('I', $nombrePdf);
        
        $this->unirReportesPdf();
        return json_encode(200);
    }

    public function unirReportesPdf(){
        $inspeccionesMdl = new InspeccionesMdl();
        $session = session();

        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);

        $ruta = ROOTPATH."public/Archivos_ETIC/inspecciones/".$datosInspeccion[0]['No_Inspeccion']."/Reportes/";
        $archivo_procedimientos_inspeccion = ROOTPATH."public/Archivos_ETIC/plantillas_reportes/F-PRS-02_PROCEDIMIENTO_INSPECCIONES.pdf";
        
        $files = array(
            $ruta."ETIC_PORTADA_REPORTE_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $ruta."ETIC_GRAFICA_ANOMALIAS_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $ruta."ETIC_INVENTARIO_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $ruta."ETIC_PROBLEMAS_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $ruta."ETIC_BASELINE_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $ruta."ETIC_LISTA_PROBLEMAS_ABIERTOS_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $ruta."ETIC_LISTA_PROBLEMAS_CERRADOS_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].".pdf",
            $archivo_procedimientos_inspeccion,            
        );
        
        $pdf = new Fpdi();
        foreach ($files as $file) {
            $pageCount = $pdf->setSourceFile($file);
            for ($pageNo=1; $pageNo <= $pageCount; $pageNo++) { 
                $template = $pdf->importPage($pageNo);
                $size = $pdf->getTemplateSize($template);
                $pdf->AddPage($size["orientation"], $size);
                $pdf->useTemplate($template);
            }
        }
        
        $nombrePdf = 'ETIC_RESULTADOS_ANALISIS_DE_RIESGO_CON_TERMOGRAFIA_INSPECCION_'.$datosInspeccion[0]['No_Inspeccion'].'.pdf';
        $pdf->Output('F', $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombrePdf);

    }

    public function generarReporteListaProblemasExcel($fecha_inicio, $fecha_fin){
        $inspeccionesMdl = new InspeccionesMdl();
        $problemasMdl = new ProblemasMdl();
        $usuariosMdl = new UsuariosMdl();
        $sitiosMdl = new SitiosMdl();
        $session = session();
        // Formato de fecha en español
        date_default_timezone_set("America/Mexico_City");
        setlocale(LC_TIME,"es_MX.UTF-8",'esp');
        // Consulta para los datos del analista termografo
        $datosInpector = $usuariosMdl->obtenerRegistros($session->Id_Usuario);
        // Consulta para los datos de la inspección
        $datosInspeccion = $inspeccionesMdl->obtenerRegistros($session->Id_Inspeccion);
        // CONSULTA DEL SITIO
        $datosSitio = $sitiosMdl->obtenerRegistros($session->Id_Sitio);

        $cliente = $datosInspeccion[0]['nombreCliente'];
        $sitio = $datosSitio[0]['Sitio'];
        $nombreGrupoSitio = $this->request->getPost('detalle_ubicacion');
        $Direccion = $datosSitio[0]['Direccion'];
        $Colonia = $datosSitio[0]['Colonia'];
        $Estado = $datosSitio[0]['Estado'];
        $Municipio = $datosSitio[0]['Municipio'];
        $direccion_completa = $Direccion.", ".$Colonia.", ".$Municipio.", ".$Estado;

        $condicion = [
            'problemas.Id_Sitio' => $session->Id_Sitio,
            'problemas.Id_Inspeccion' => $session->Id_Inspeccion,
        ];
        $orden = 'Id_Tipo_Inspeccion ASC, Numero_Problema ASC';
        $problemas = $problemasMdl->getProblemas_Sitio($condicion, $orden);

        if($fecha_inicio == $fecha_fin){
            $fecha_inicio_format = strftime('%e de %B del %Y', strtotime($fecha_inicio));
            
            $strFecha = $fecha_inicio_format;
        }else{
            $fecha_inicio_format = strftime('%e de %B', strtotime($fecha_inicio));
            $fecha_fin_format = strftime('%e de %B del %Y', strtotime($fecha_fin));

            $strFecha = "Del ".$fecha_inicio_format." al ".$fecha_fin_format;
        }

        $spreadsheet = \PhpOffice\PhpSpreadsheet\IOFactory::load(ROOTPATH."public/Archivos_ETIC/plantillas_reportes/LISTA_PROBLEMAS.xlsx");
        $activeWorksheet = $spreadsheet->getActiveSheet();

        $activeWorksheet->getCell('L4')->setValue("Cliente: ".$cliente." / ".$sitio);
        $activeWorksheet->getCell('L5')->setValue("Fecha Servicio: ".$strFecha);

        $numero_columna= 9;
        foreach ($problemas as $problema) {
            $spreadsheet->getSheet(0)->setCellValue("A" . $numero_columna, $problema["numInspeccion"]);
            $spreadsheet->getSheet(0)->setCellValue("B" . $numero_columna, $problema["Numero_Problema"]);
            $spreadsheet->getSheet(0)->setCellValue("C" . $numero_columna, $problema["nombre_sitio"]);
            $spreadsheet->getSheet(0)->setCellValue("D" . $numero_columna, $problema["tipoInspeccion"]);
            $spreadsheet->getSheet(0)->setCellValue("E" . $numero_columna, $problema["Ruta"]);
            $spreadsheet->getSheet(0)->setCellValue("F" . $numero_columna, $problema["severidad"]);
            if($problema["Problem_Temperature"] != null){
                $spreadsheet->getSheet(0)->setCellValue("G" . $numero_columna, $problema["Problem_Temperature"]);
            }else{
                $spreadsheet->getSheet(0)->setCellValue("G" . $numero_columna, " ");
            }
            if($problema["Reference_Temperature"] != null){
                $spreadsheet->getSheet(0)->setCellValue("H" . $numero_columna, $problema["Reference_Temperature"]);
            }else{
                $spreadsheet->getSheet(0)->setCellValue("H" . $numero_columna, " ");
            }
            $spreadsheet->getSheet(0)->setCellValue("I" . $numero_columna, $problema["Estatus_Problema"]);
            $spreadsheet->getSheet(0)->setCellValue("J" . $numero_columna, $problema["Es_Cronico"]);
            $spreadsheet->getSheet(0)->setCellValue("K" . $numero_columna, $problema["Fecha_Creacion_formateada"]);
            if($problema["Component_Comment"] != null){
                $spreadsheet->getSheet(0)->setCellValue("L" . $numero_columna, $problema["Component_Comment"]);
            }else{
                $spreadsheet->getSheet(0)->setCellValue("L" . $numero_columna, " ");
            }
            $numero_columna++;
        }

        $writer = \PhpOffice\PhpSpreadsheet\IOFactory::createWriter($spreadsheet, 'Xlsx');
        
        $nombreExcel = 'ETIC_LISTADO_DE_PROBLEMAS_'.$sitio."_INSPECCION_".$datosInspeccion[0]['No_Inspeccion'].'.Xlsx';

        $this->validar_carpeta_creada($_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/');

        $writer->save($_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$datosInspeccion[0]['No_Inspeccion'].'/Reportes/'.$nombreExcel);

        return json_encode(200);
    }
    
    function validar_carpeta_creada($ruta_carpeta){
        $session = session();

        if(!file_exists($ruta_carpeta)){
            mkdir($ruta_carpeta,0777,true);
        }
        return;
    }

    function abrir_carpeta_archivos(){
        $session = session();

        $ruta = $this->request->getPost('ruta');

        switch ($ruta) {
            case "imagenes":
                if ($session->inspeccion == '') {
                    return json_encode(500);
                }
        
                $ruta_carpeta = $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$session->inspeccion.'/Imagenes';
                $this->validar_carpeta_creada($ruta_carpeta);
            break;
            case "reportes":
                if ($session->inspeccion == '') {
                    return json_encode(500);
                }
        
                $ruta_carpeta = $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$session->inspeccion.'/Reportes';
                $this->validar_carpeta_creada($ruta_carpeta);
            break;
            case "documentacion":
                $ruta_carpeta = $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/Documentación';
            break;
        }
        
        return shell_exec("start ".$ruta_carpeta);
    }

    function guardar_datos_reporte(){

        $datos_reporte = new DatosReporteMdl();
        $session = session();

        $array_problemas = $this->request->getPost('arrayProblemasSeleccionados');
        $str_problemas_seleccionados = $array_problemas != "" ? implode(",", $this->request->getPost('arrayProblemasSeleccionados')) : "";

        $ultimo_registro_en_bd = $datos_reporte->get();
        $data = [
            'Id_Inspeccion' => $session->Id_Inspeccion,
            'Id_Sitio' => $session->Id_Sitio,
            'detalle_ubicacion' => $this->request->getPost('detalle_ubicacion'),
            'nombre_contacto' => implode('$', $this->request->getPost('nombre_contacto')),
            'puesto_contacto' => implode('$', $this->request->getPost('puesto_contacto')),
            'fecha_inicio_ra' => $this->request->getPost('fecha_inicio_ra'),
            'fecha_fin_ra' => $this->request->getPost('fecha_fin_ra'),
            'nombre_img_portada' => $this->request->getPost('nombre_img_portada'),
            'descripcion_reporte' => implode('$', $this->request->getPost('descripcion_reporte')),
            'areas_inspeccionadas' => implode('$', $this->request->getPost('areas_inspeccionadas')),
            'recomendacion_reporte' => implode('$', $this->request->getPost('recomendacion_reporte')),
            'imagen_recomendacion' => implode('$', $this->request->getPost('imagen_recomendacion')),
            'imagen_recomendacion_2' => implode('$', $this->request->getPost('imagen_recomendacion_2')),
            'referencia_reporte' => implode("$", $this->request->getPost('referencia_reporte')),
            'arrayElementosSeleccionados' => implode(",", $this->request->getPost('arrayElementosSeleccionados')),
            'arrayProblemasSeleccionados' => $str_problemas_seleccionados,
        ];
        

        if(is_null($ultimo_registro_en_bd[0]['ultimo_registro'])){
            $saveProblema = $datos_reporte->insert($data);
        }else{
            $updateProblema = $datos_reporte->update($ultimo_registro_en_bd[0]['ultimo_registro'],$data);
        }
        
        return;
    }

    function obtener_datos_reporte(){
        $datos_reporte = new DatosReporteMdl();
        $session = session();

        return json_encode($datos_reporte->get_registros($session->Id_Inspeccion));
    }

    function optimizar_imagenes(){
        $session = session();

        // Validamos que la carpeta para las imagenes optimizadas exista, si no, la creamos
        $ruta_carpeta = $_SERVER["DOCUMENT_ROOT"].'/Archivos_ETIC/inspecciones/'.$session->inspeccion.'/Imagenes_optimizadas';
        $this->validar_carpeta_creada($ruta_carpeta);

        $ruta = ROOTPATH.'public/Archivos_ETIC/inspecciones/'.$session->inspeccion.'/Imagenes';
        $imagenesArray = array();

        // Abre un gestor de directorios para la ruta indicada
        $gestor = opendir($ruta);

        // Recorre todos los elementos del directorio
        while (($archivo = readdir($gestor)) !== false)  {
            // Se muestran todos los archivos y carpetas excepto "." y ".."
            if ($archivo != "." && $archivo != "..") {
                array_push($imagenesArray,$archivo);
            }
        }

        closedir($gestor);

        foreach ($imagenesArray as $key => $nombre_img_original) {
            $ruta_imagen_original = ROOTPATH."public/Archivos_ETIC/inspecciones/".$session->inspeccion."/Imagenes/".$nombre_img_original; //Imagen original
            // return $ruta_imagen_original;
            $ruta_img_nueva = ROOTPATH."public/Archivos_ETIC/inspecciones/".$session->inspeccion."/Imagenes_optimizadas/".$nombre_img_original; //Nueva imagen
            $ancho = 500; //Nuevo ancho 440 409
            $alto = 375;  //Nuevo alto 330 307
            //Creamos una nueva imagen a partir del fichero inicial
            
            $ruta_imagen_original = imagecreatefromjpeg($ruta_imagen_original);
            //Obtenemos el tamaño 
            $x = imagesx($ruta_imagen_original);
            $y = imagesy($ruta_imagen_original);
            
            if ($x >= $y) {
                $ancho = $ancho;
                $alto = $ancho * $y / $x;
                } else {
                $alto = $alto;
                $ancho = $x / $y * $alto;
            }

            $img_optimizada = imagecreatetruecolor($ancho, $alto);
            imagecopyresampled($img_optimizada, $ruta_imagen_original, 0, 0, 0, 0, floor($ancho), floor($alto), $x, $y);
            
            //se crea la imagen
            imagejpeg($img_optimizada, $ruta_img_nueva,90);

            imagedestroy($ruta_imagen_original);
            imagedestroy($img_optimizada);
        }

        return json_encode(200);
    }
}

class PDF extends FPDF{

    // ENCABEZADOS Y PIE DE PAGINA
    public $datosEncabezado;

    public function __construct($datosEncabezado) {
        parent::__construct();
        $this->datosEncabezado = $datosEncabezado;
    }

    // Cabecera de página
    function Header(){
        // Logo
        $this->Image(base_url('/img/sistema/ETIC_logo.jpg'),11,10,38);
        // Arial bold 15
        $this->SetFont('Arial','B',13);
        // Titulo
        $this->SetXY(86,10);
        $this->MultiCell(125,6,utf8_decode($this->datosEncabezado["titulo"]),0,'C');

        $this->SetXY(10,25);

        switch ($this->datosEncabezado["tipoEncabezado"]) {
            case 1:
                $this->encabezadoReporteInventario();
            break;
            case 4:
                // Logo CLientes
                // $this->Rect(247,10,40,14);
                if(is_dir(ROOTPATH."public/".$this->datosEncabezado["ruta_imagen_cliente"]) != true && file_exists(ROOTPATH."public/".$this->datosEncabezado["ruta_imagen_cliente"])){
                    $this->Image(base_url($this->datosEncabezado["ruta_imagen_cliente"]),247,8,40,16);
                }
                $this->encabezadoListaProblemas();
            break;
            case 5:
                // Logo CLientes
                // $this->Rect(247,8,40,16);
                if(is_dir(ROOTPATH."public/".$this->datosEncabezado["ruta_imagen_cliente"]) != true && file_exists(ROOTPATH."public/".$this->datosEncabezado["ruta_imagen_cliente"])){
                    $this->Image(base_url($this->datosEncabezado["ruta_imagen_cliente"]),247,8,40,16);
                }
            break;
        }


    }

    function encabezadoReporteInventario(){
        // Apartado De datos de la inspeccion
        $this->SetY($this->GetY()+2);

        if($this->datosEncabezado["grupo"] != ""){
            $this->SetFont('Arial','B',8); $this->Cell(80,4,utf8_decode($this->datosEncabezado["grupo"]),0,0,'L') ;$this->Ln();
        }
        $this->SetFont('Arial','B',8); $this->Cell(80,4,utf8_decode($this->datosEncabezado["cliente"]),0,0,'L'); $this->Ln();
        $this->SetFont('Arial','',8); $this->Cell(80,4,utf8_decode($this->datosEncabezado["sitio"]),0,0,'L'); $this->Ln();
        $this->Cell(80,4,utf8_decode('Analista Termógrafo: '.$this->datosEncabezado["analistaTermografo"]),0,0,'L'); $this->Ln();
        $this->Cell(80,4,utf8_decode('Nivel De Certificación: '.$this->datosEncabezado['nivelCertificacion']),0,0,'L'); $this->Ln();
        $this->Cell(80,4, 'Fecha De Reporte: '.date("Y/m/d"),0,0,'L'); $this->Ln();
        
        // Datos inspeccion actual y anterior
        $this->SetXY(97, 27);
        $this->SetFont('Arial','B',8); $this->Cell(34,4,utf8_decode('No. Inspección Anterior:'),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(10,4,utf8_decode($this->datosEncabezado['inspeccionAnterior']),0,0,'L');$this->Ln();
        $this->SetX(97);
        $this->SetFont('Arial','B',8); $this->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(15,4,utf8_decode($this->datosEncabezado["fecha_inspeccion_anterior"]),0,0,'L');$this->Ln();
        $this->SetX(97);
        $this->SetFont('Arial','B',8);$this->Cell(34,4,utf8_decode('No. Inspección Actual:'),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(10,4,utf8_decode($this->datosEncabezado['inspeccionActual']),0,0,'L');$this->Ln();
        $this->SetX(97);
        $this->SetFont('Arial','B',8); $this->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(15,4,utf8_decode($this->datosEncabezado["fecha_inspeccion_actual"]),0,0,'L');$this->Ln();

        // Recuadro Tipo de problema
        $this->SetXY(150, 27); $this->SetFont('Arial','B',8); $this->Cell(30,4,'Tipo De Problema',1,0,'L'); $this->Ln();
        $this->SetX(150); $this->SetFont('Arial','',8); $this->Cell(30,4,utf8_decode('E = Eléctrico'),'L,R',0,'L'); $this->Ln();
        $this->SetX(150);$this->Cell(30,4,utf8_decode('M = Mecánico'),'L,R',0,'L'); $this->Ln();
        $this->SetX(150);$this->Cell(30,4,'V = Visual','L,R,B',0,'L'); $this->Ln();

        // Recuadro de Prioridad Operatica
        $this->SetXY(189, 27); $this->SetFont('Arial','B',8); $this->Cell(40,4,'Prioridad Operativa',1,0,'L'); $this->Ln();
        $this->SetX(189); $this->SetFont('Arial','',8); $this->Cell(40,4,utf8_decode('CTO = Crítico'),'L,R',0,'L'); $this->Ln();
        $this->SetX(189);$this->Cell(40,4,utf8_decode('ETO = Esencial'),'L,R',0,'L'); $this->Ln();
        $this->SetX(189);$this->Cell(40,4,'UN = No clasificado','L,R,B',0,'L'); $this->Ln();

        // Recuadro de Estado de Equipo en Inspección
        // $this->SetXY(237, 27); $this->SetFont('Arial','B',8); $this->Cell(50,4,utf8_decode('Estado De Equipo En Inspección'),1,0,'L'); $this->Ln();
        // $this->SetX(237); $this->SetFont('Arial','',8); $this->Cell(50,4,utf8_decode('TBT = A Inspeccionar'),'L,R',0,'L'); $this->Ln();
        // $this->SetX(237);$this->Cell(50,4,'TESTED = Inspeccionado','L,R',0,'L'); $this->Ln();
        // $this->SetX(237);$this->Cell(50,4,utf8_decode('NT/NL = Sin Carga'),'L,R',0,'L'); $this->Ln();
        // $this->SetX(237);$this->Cell(50,4,'NT/UR = En Mantenimiento','L,R',0,'L'); $this->Ln();
        // $this->SetX(237);$this->Cell(50,4,'NT/LO = Bloqueado','L,R',0,'L'); $this->Ln();
        // $this->SetX(237);$this->Cell(50,4,'NT/NA = No Disponible','L,R,B',0,'L'); $this->Ln();

        $this->SetXY(237, 27); $this->SetFont('Arial','B',8); $this->Cell(50,4,utf8_decode('Estado De Equipo En Inspección'),1,0,'L'); $this->Ln();
        $this->SetX(237); $this->SetFont('Arial','',8); $this->Cell(50,4,utf8_decode('PVERIF = Para Verificar'),'L,R',0,'L'); $this->Ln();
        $this->SetX(237);$this->Cell(50,4,'VERIFICADO = Verificado','L,R',0,'L'); $this->Ln();
        $this->SetX(237);$this->Cell(50,4,utf8_decode('NOCARGA = Sin Carga'),'L,R',0,'L'); $this->Ln();
        $this->SetX(237);$this->Cell(50,4,'MTTO = En Mantenimiento','L,R',0,'L'); $this->Ln();
        $this->SetX(237);$this->Cell(50,4,'BLOQ = Bloqueado','L,R',0,'L'); $this->Ln();
        $this->SetX(237);$this->Cell(50,4,'NOACC = No Accesible','L,R,B',0,'L'); $this->Ln();

        // Ubicando la tabla XY
        $this->SetXY(10, 60);

        // Encabezados de la tabla
        $header = array('Estado', 'Prioridad','# Problema', ' Ubicación', 'Código Barras','Notas');
        // Encabezados de la tabala con Negritas
        $this->SetFont('Arial','B',8);
        for($i=0;$i<count($header);$i++)
            $this->Cell($this->datosEncabezado["anchoColumnas"][$i],5,utf8_decode($header[$i]),"B",0,'L');
        $this->Ln();
    }

    function encabezadoListaProblemas(){
        $this->SetY($this->GetY()+2);
        // Apartado De datos de la inspeccion
        if($this->datosEncabezado["grupo"] != ""){
            $this->SetFont('Arial','B',8); $this->Cell(80,4,utf8_decode($this->datosEncabezado["grupo"]),0,0,'L') ;$this->Ln();
        }
        $this->SetFont('Arial','B',8); $this->Cell(100,4,utf8_decode($this->datosEncabezado["cliente"]),0,0,'L'); $this->Ln();
        $this->SetFont('Arial','',8); $this->Cell(100,4,utf8_decode($this->datosEncabezado["sitio"]),0,0,'L'); $this->Ln();
        $this->Cell(100,4,utf8_decode('Analista Termógrafo: '.$this->datosEncabezado["analistaTermografo"]),0,0,'L'); $this->Ln();
        $this->Cell(102,4,utf8_decode('Nivel De Certificación: '.$this->datosEncabezado['nivelCertificacion']),0,0,'L'); $this->Ln();

        $this->SetXY(244, 27);
        $this->Cell(44,4, 'Fecha De Reporte: '.date("d/m/Y"),0,0,'R'); $this->Ln();
        // $this->SetX(242); $this->Cell(45,4,utf8_decode('No. Inspección Anterior: '.$this->datosEncabezado["inspeccionAnterior"]),0,0,'R'); $this->Ln();
        // $this->SetX(242); $this->Cell(45,4,utf8_decode('No. Inspección Actual: '.$this->datosEncabezado["inspeccionActual"]),0,0,'R'); $this->Ln();

        // Datos inspeccion actual y anterior
        $this->SetX(218);
        $this->SetFont('Arial','',8); $this->Cell(34,4,utf8_decode('No. Inspección Anterior:'),0,0,'R');
        $this->SetFont('Arial','',8); $this->Cell(10,4,utf8_decode($this->datosEncabezado['inspeccionAnterior']),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(15,4,utf8_decode($this->datosEncabezado["fecha_inspeccion_anterior"]),0,0,'L');$this->Ln();
        $this->SetX(218);
        $this->SetFont('Arial','',8);$this->Cell(34,4,utf8_decode('No. Inspección Actual:'),0,0,'R');
        $this->SetFont('Arial','',8); $this->Cell(10,4,utf8_decode($this->datosEncabezado['inspeccionActual']),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(10,4,utf8_decode("Fecha: "),0,0,'L');
        $this->SetFont('Arial','',8); $this->Cell(15,4,utf8_decode($this->datosEncabezado["fecha_inspeccion_actual"]),0,0,'L');$this->Ln();

        // Encabezados de la tabla
        $header = array(
            "Equipo / Comentarios",
            "Fecha",
            "No. Insp",
            "# Problema",
            "Estatus",
            "Crónico",
            "Temp",
            "DeltaT",
            "Severidad",
        );

        // Ubicando encabezados de la tabla XY
        $this->SetXY(10, 50);

        // Encabezados de la tabala con Negritas
        $this->SetFont('Arial','B',8);
        for($i=0;$i<count($header);$i++)
            $this->Cell($this->datosEncabezado["anchoColumnas"][$i],7,utf8_decode($header[$i]),"B",0,$this->datosEncabezado["alineacionEncabezados"][$i]);
        $this->Ln();
    }

    // Pie de página
    function Footer(){
        // Arial italic 8
        $this->SetFont('Arial','',7);
        // Número de página
        $this->SetY(195);
        $this->Cell(0,4,utf8_decode('ETIC SA DE CV'),0,0,'C'); $this->Ln();
        $this->Cell(0,4,utf8_decode('Copyright © 2023 NefWorks Todos los derechos reservados.'),0,0,'C');

        // Posición: a 1,5 cm del final
        $this->SetY(-15);
        // $this->Cell(0,10,utf8_decode('Página '.$this->PageNo().'/{nb}'),0,'L',0);
    }

    // APARTADO PARA GRAFICAS

    function LineGraph($w, $h, $data, $options='', $colors=null, $maxVal=0, $nbDiv=4){
        /*******************************************
        Explain the variables:
        $w = the width of the diagram
        $h = the height of the diagram
        $data = the data for the diagram in the form of a multidimensional array
        $options = the possible formatting options which include:
            'V' = Print Vertical Divider lines
            'H' = Print Horizontal Divider Lines
            'kB' = Print bounding box around the Key (legend)
            'vB' = Print bounding box around the values under the graph
            'gB' = Print bounding box around the graph
            'dB' = Print bounding box around the entire diagram
        $colors = A multidimensional array containing RGB values
        $maxVal = The Maximum Value for the graph vertically
        $nbDiv = The number of vertical Divisions
        *******************************************/
        // $this->SetDrawColor(0,0,0);
        $this->SetDrawColor(171,171,171);
        $this->SetLineWidth(0.2);
        $keys = array_keys($data);
        $ordinateWidth = 10;
        $w -= $ordinateWidth;
        $valX = $this->getX()+$ordinateWidth;
        $valY = $this->getY();
        $margin = 1;
        $titleH = 8;
        $titleW = $w;
        $lineh = 4;
        $keyH = count($data)*$lineh;
        $keyW = $w/5;
        $graphValH = 4;
        $graphValW = $w-$keyW-3*$margin;
        $graphH = $h-(3*$margin)-$graphValH;
        $graphW = $w;
        $graphX = $valX+$margin;
        $graphY = $valY+$margin;
        $graphValX = $valX+$margin;
        $graphValY = $valY+2*$margin+$graphH;
        $keyX = $valX+(2*$margin)+$graphW;
        $keyY = $valY+$margin+.5*($h-(2*$margin))-.5*($keyH);
        //Borde externo de todo el diagrama
        if(strstr($options,'gB')){
            $this->Rect($valX-5,$valY-6,$w+7,$h+6);
        }
        //borde de la grafica
        if(strstr($options,'dB')){
            $this->Rect($valX+$margin,$valY+$margin,$w,$graphH);
        }
        //borde de las Keys
        if(strstr($options,'kB')){
            $this->Rect($graphValX,$valY -5,$w,4);

            $this->SetXY($graphValX-7,$valY -6);
            $this->SetFont('Arial','',10);
            $this->Cell(7,6,utf8_decode("°C"),0,0,'R');
        }
        //draw graph value box
        if(strstr($options,'vB')){
            $this->Rect($graphValX-5,$graphValY,$w+5,$graphValH);
        }
        //define colors
        if($colors===null){
            $safeColors = array(0,51,102,153,204,225);
            for($i=0;$i<count($data);$i++){
                $colors[$keys[$i]] = array($safeColors[array_rand($safeColors)],$safeColors[array_rand($safeColors)],$safeColors[array_rand($safeColors)]);
            }
        }
        //form an array with all data values from the multi-demensional $data array
        $ValArray = array();
        foreach($data as $key => $value){
            foreach($data[$key] as $val){
                $ValArray[]=$val;
            }
        }
        //define max value
        if($maxVal<ceil(max($ValArray))){
            $maxVal = ceil(max($ValArray));
        }
        //dibuja lineas horizontakes
        $vertDivH = $graphH/$nbDiv;
        if(strstr($options,'H')){
            for($i=0;$i<=$nbDiv;$i++){
                if($i<$nbDiv){
                    $this->Line($graphX,$graphY+$i*$vertDivH,$graphX+$graphW,$graphY+$i*$vertDivH);
                } else{
                    $this->Line($graphX,$graphY+$graphH,$graphX+$graphW,$graphY+$graphH);
                }
            }
        }

        //dibuja lineas verticales
        // VALIDACION PARA EVITAR LA DIVICION ENTRE CERO
        $totalDiviciones = count($data[$keys[0]]);
        $totalDiviciones = $totalDiviciones == 1 ? 1 : $totalDiviciones - 1;

        $horiDivW = floor($graphW/$totalDiviciones);
        if(strstr($options,'V')){
            for($i=0;$i<=$totalDiviciones;$i++){
                if($i<$totalDiviciones){
                    $this->Line($graphX+$i*$horiDivW,$graphY,$graphX+$i*$horiDivW,$graphY+$graphH);
                } else {
                    $this->Line($graphX+$graphW,$graphY,$graphX+$graphW,$graphY+$graphH);
                }
            }
        }
        //draw graph lines
        $sp = 0;
        foreach($data as $key => $value){
            // color de las lineas de la grafica
            $this->setDrawColor($colors[$key][0],$colors[$key][1],$colors[$key][2]);
            // grosor de las lineas de la grafica
            $this->SetLineWidth(0.5);
            $valueKeys = array_keys($value);

            // DIBUJANDO LOS CIRCULOS INICIALES
            $this->SetFillColor($colors[$key][0],$colors[$key][1],$colors[$key][2]);

            for($i=0;$i<count($value);$i++){
                $abc = 0;
                if($i==count($value)-2){
                    $this->Line(
                        $graphX+($i*$horiDivW),
                        $graphY+$graphH-($value[$valueKeys[$i]]/$maxVal*$graphH),
                        $graphX+$graphW,
                        $graphY+$graphH-($value[$valueKeys[$i+1]]/$maxVal*$graphH)
                    );
                    $posicionX = $graphX+($i*$horiDivW);
                    $posicionY = $graphY+$graphH-($value[$valueKeys[$i]]/$maxVal*$graphH);
                } else if($i<(count($value)-1)) {
                    $this->Line(
                        $graphX+($i*$horiDivW),
                        $graphY+$graphH-($value[$valueKeys[$i]]/$maxVal*$graphH),
                        $graphX+($i+1)*$horiDivW,
                        $graphY+$graphH-($value[$valueKeys[$i+1]]/$maxVal*$graphH)
                    );
                    $posicionX = $graphX+($i*$horiDivW);
                    $posicionY = $graphY+$graphH-($value[$valueKeys[$i]]/$maxVal*$graphH);
                }else{
                    $posicionX = $graphX+($i*$horiDivW)+($i-1);
                    $posicionY = $graphY+$graphH-($value[$valueKeys[$i]]/$maxVal*$graphH);
                }

                if($i <= 0 && count($value) <= 1 ){
                    $abc = 1;
                }

                $this->Circle($posicionX+$abc,$posicionY,(1/2),'D');
                $this->SetFillColor(243,255,0);
                $this->Circle($posicionX+$abc,$posicionY,(1/2),'F');
            }

            //Dibuja las llaves de la grafica
            $this->SetFont('Arial','',7);
            if(!isset($n))$n=0;
            $this->Line(((10*($sp+1)) + ($graphValX + (15*$sp))-5),$valY -3,(10*($sp+1)) + ($graphValX + (15*$sp)),$valY-3);
            $this->SetXY((10*($sp+1)) + ($graphValX + (15*$sp)),$valY-5);
            $this->Cell(15,$lineh,utf8_decode($key),0,0,'L');
            $n++;
            $sp++;
        }
        // VALORES DE X INSPECCIONES
        //print the abscissa values
        foreach($valueKeys as $key => $value){

            // Si se agrego numero inspeccion y fecha para el ordenamiento, aquí se quita el numero inspeccion
            // para pintar solo la fecha delas inspecciones
            if(str_contains($value,"-")){
                $value = substr($value,-10);
            }

            if($key==0){
                $this->SetXY($graphValX-5,$graphValY);
                $this->Cell(10,$lineh,$value,0,0,'L');
            } else if($key==count($valueKeys)-1){
                $this->SetXY($graphValX+$w-10,$graphValY);
                $this->Cell(10,$lineh,$value,0,0,'R');
            } else {
                $this->SetXY($graphValX+$key*$horiDivW-5,$graphValY);
                $this->Cell(10,$lineh,$value,0,0,'C');
            }
        }
        // VALORES DE Y VALORES
        //print the ordinate values
        for($i=0;$i<=$nbDiv;$i++){
            $this->SetXY($graphValX-7,$graphY+($nbDiv-$i)*$vertDivH-3);
            $this->Cell(7,6,floor($maxVal/$nbDiv*$i),0,0,'R');
        }
        $this->SetDrawColor(0,0,0);
        $this->SetLineWidth(0.2);
    }

    function Circle($x, $y, $r, $style='D'){
        $this->Ellipse($x,$y,$r,$r,$style);
    }

    function Ellipse($x, $y, $rx, $ry, $style='D'){
        if($style=='F')
            $op='f';
        elseif($style=='FD' || $style=='DF')
            $op='B';
        else
            $op='S';
        $lx=4/3*(M_SQRT2-1)*$rx;
        $ly=4/3*(M_SQRT2-1)*$ry;
        $k=$this->k;
        $h=$this->h;
        $this->_out(sprintf('%.2F %.2F m %.2F %.2F %.2F %.2F %.2F %.2F c',
            ($x+$rx)*$k,($h-$y)*$k,
            ($x+$rx)*$k,($h-($y-$ly))*$k,
            ($x+$lx)*$k,($h-($y-$ry))*$k,
            $x*$k,($h-($y-$ry))*$k));
        $this->_out(sprintf('%.2F %.2F %.2F %.2F %.2F %.2F c',
            ($x-$lx)*$k,($h-($y-$ry))*$k,
            ($x-$rx)*$k,($h-($y-$ly))*$k,
            ($x-$rx)*$k,($h-$y)*$k));
        $this->_out(sprintf('%.2F %.2F %.2F %.2F %.2F %.2F c',
            ($x-$rx)*$k,($h-($y+$ly))*$k,
            ($x-$lx)*$k,($h-($y+$ry))*$k,
            $x*$k,($h-($y+$ry))*$k));
        $this->_out(sprintf('%.2F %.2F %.2F %.2F %.2F %.2F c %s',
            ($x+$lx)*$k,($h-($y+$ry))*$k,
            ($x+$rx)*$k,($h-($y+$ly))*$k,
            ($x+$rx)*$k,($h-$y)*$k,
            $op));
    }

    // APARTADO PARA TABLA DE MULTICELDAS
    protected $widths;
    protected $aligns;
    protected $bgColor;

    function SetWidths($w)
    {
        // Set the array of column widths
        $this->widths = $w;
    }

    function SetAligns($a)
    {
        // Set the array of column alignments
        $this->aligns = $a;
    }

    function SetColorCell($cl){
        $this->bgColor = $cl;
    }

    function Row($data)
    {
        // Calculate the height of the row
        $nb = 0;
        for($i=0;$i<count($data);$i++)
            $nb = max($nb,$this->NbLines($this->widths[$i],$data[$i]));
        $h = 5*$nb;
        // Issue a page break first if needed
        $this->CheckPageBreak($h);
        // Draw the cells of the row
        for($i=0;$i<count($data);$i++)
        {
            $w = $this->widths[$i];
            $a = isset($this->aligns[$i]) ? $this->aligns[$i] : 'L';
            // Save the current position
            $x = $this->GetX();
            $y = $this->GetY();
            // COlor de fondo de la celda
            $colorR = isset($this->bgColor[0]) ? $this->bgColor[0] : 255;
            $colorG = isset($this->bgColor[0]) ? $this->bgColor[1] : 255;
            $colorB = isset($this->bgColor[0]) ? $this->bgColor[2] : 255;

            $this->SetFillColor($colorR, $colorG, $colorB);
            // Draw the border
            $this->Rect($x,$y,$w,$h,"F");


            if ($data[$i] === "SI"  || $data[$i] === "Crítico") {
                $this->SetTextColor(255,0,0);
                $this->SetFont('Arial','B',8);
            }
            // Print the text
            $this->MultiCell($w,4,utf8_decode($data[$i]),0,$a);

            $this->SetTextColor(0,0,0);
            $this->SetFont('Arial','',8);
            // Put the position to the right of the cell
            $this->SetXY($x+$w,$y);
        }
        // Go to the next line
        $this->Ln($h);
    }

    function CheckPageBreak($h)
    {
        // If the height h would cause an overflow, add a new page immediately
        if($this->GetY()+$h>$this->PageBreakTrigger)
            $this->AddPage($this->CurOrientation);
            $y = $this->GetY()+1;
            $this->SetY($y);

    }

    function NbLines($w, $txt)
    {
        // Compute the number of lines a MultiCell of width w will take
        if(!isset($this->CurrentFont))
            $this->Error('No font has been set');
        $cw = $this->CurrentFont['cw'];
        if($w==0)
            $w = $this->w-$this->rMargin-$this->x;
        $wmax = ($w-2*$this->cMargin)*1000/$this->FontSize;
        $s = str_replace("\r",'',(string)$txt);
        $nb = strlen($s);
        if($nb>0 && $s[$nb-1]=="\n")
            $nb--;
        $sep = -1;
        $i = 0;
        $j = 0;
        $l = 0;
        $nl = 1;
        while($i<$nb)
        {
            $c = $s[$i];
            if($c=="\n")
            {
                $i++;
                $sep = -1;
                $j = $i;
                $l = 0;
                $nl++;
                continue;
            }
            if($c==' ')
                $sep = $i;
            $l += $cw[$c];
            if($l>$wmax)
            {
                if($sep==-1)
                {
                    if($i==$j)
                        $i++;
                }
                else
                    $i = $sep+1;
                $sep = -1;
                $j = $i;
                $l = 0;
                $nl++;
            }
            else
                $i++;
        }
        return $nl;
    }

    // PRUEBA
    function Sector($xc, $yc, $r, $a, $b, $style='FD', $cw=true, $o=90)
    {
        $d0 = $a - $b;
        if($cw){
            $d = $b;
            $b = $o - $a;
            $a = $o - $d;
        }else{
            $b += $o;
            $a += $o;
        }
        while($a<0)
            $a += 360;
        while($a>360)
            $a -= 360;
        while($b<0)
            $b += 360;
        while($b>360)
            $b -= 360;
        if ($a > $b)
            $b += 360;
        $b = $b/360*2*M_PI;
        $a = $a/360*2*M_PI;
        $d = $b - $a;
        if ($d == 0 && $d0 != 0)
            $d = 2*M_PI;
        $k = $this->k;
        $hp = $this->h;
        if (sin($d/2))
            $MyArc = 4/3*(1-cos($d/2))/sin($d/2)*$r;
        else
            $MyArc = 0;
        //first put the center
        $this->_out(sprintf('%.2F %.2F m',($xc)*$k,($hp-$yc)*$k));
        //put the first point
        $this->_out(sprintf('%.2F %.2F l',($xc+$r*cos($a))*$k,(($hp-($yc-$r*sin($a)))*$k)));
        //draw the arc
        if ($d < M_PI/2){
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
        }else{
            $b = $a + $d/4;
            $MyArc = 4/3*(1-cos($d/8))/sin($d/8)*$r;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
            $a = $b;
            $b = $a + $d/4;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
            $a = $b;
            $b = $a + $d/4;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
            $a = $b;
            $b = $a + $d/4;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
        }
        //terminate drawing
        if($style=='F')
            $op='f';
        elseif($style=='FD' || $style=='DF')
            $op='b';
        else
            $op='s';
        $this->_out($op);
    }

    function _Arc($x1, $y1, $x2, $y2, $x3, $y3 )
    {
        $h = $this->h;
        $this->_out(sprintf('%.2F %.2F %.2F %.2F %.2F %.2F c',
            $x1*$this->k,
            ($h-$y1)*$this->k,
            $x2*$this->k,
            ($h-$y2)*$this->k,
            $x3*$this->k,
            ($h-$y3)*$this->k));
    }

    function SetDash($black=null, $white=null)
    {
        if($black!==null)
            $s=sprintf('[%.3F %.3F] 0 d',$black*$this->k,$white*$this->k);
        else
            $s='[] 0 d';
        $this->_out($s);
    }
}

class PDF_RA extends FPDF{

    public function __construct() {
        parent::__construct();
    }

    // Cabecera de página
    function Header(){
        if ( $this->PageNo() !== 1 ) {
            // Logo
            // $this->SetAlpha(0.5);
            $this->Image(base_url('/img/sistema/ETIC_logo.jpg'),147,10,38);
            // $this->SetAlpha(1);
            // Arial bold 15
        }
    }

    // Pie de página
    function Footer(){
        // if ( $this->PageNo() !== 1 ) {
            // $this->SetAlpha(0.7);
            $this->SetTextColor(0,0,0);
            $this->SetFont('Arial','',8);
            // $this->SetY(195);
            $this->SetY(-30);
            $this->SetLineWidth(0);
            // $this->Cell(170,0,'','T');$this->Ln();
            $this->Line($this->GetX(),$this->GetY()-2,210-$this->GetX(),$this->GetY()-2);

            $this->Cell(85,4,utf8_decode("Especialistas en Termografía Industrial y Corporativa, S.A. de C.V."),0,0,"L"); $this->Cell(0,4,utf8_decode("Sucursal Bajío"),0,0,"R"); $this->Ln();

            $this->Cell(85,4,utf8_decode("Sucursal matriz"),0,0,"L"); $this->Cell(0,4,utf8_decode("Col. El Dorado, C.P. 37590"),0,0,"R"); $this->Ln();
            $this->Cell(85,4,utf8_decode("Col. Las Américas, C.P. 55076"),0,0,"L"); $this->Cell(0,4,utf8_decode("León, Guanajuato"),0,0,"R"); $this->Ln();
            $this->Cell(0,4,utf8_decode("Ecatepec de Morelos, Estado de México"),0,0,"L"); $this->Cell(0,4,utf8_decode("Teléfonos: 55 8032 5401"),0,0,"R"); $this->Ln();
            $this->Cell(0,4,utf8_decode("Teléfonos: 55 8032 5401"),0,0,"L"); $this->Cell(0,4,utf8_decode("F-PRS-02"),0,0,"R"); $this->Ln();

            // $this->SetAlpha(1);
            // Número de página
            // $this->Cell(0,10,utf8_decode('Página '.$this->PageNo().'/{nb}'),0,'L',0);
        // }
    }

    // APARTADO PARA TABLA DE MULTICELDAS
    protected $widths;
    protected $aligns;
    protected $bgColor;

    function SetWidths($w)
    {
        // Set the array of column widths
        $this->widths = $w;
    }

    function SetAligns($a)
    {
        // Set the array of column alignments
        $this->aligns = $a;
    }

    function SetColorCell($cl){
        $this->bgColor = $cl;
    }

    function Row($data)
    {
        // Calculate the height of the row
        $nb = 0;
        for($i=0;$i<count($data);$i++)
            $nb = max($nb,$this->NbLines($this->widths[$i],$data[$i]));
        $h = 5*$nb;
        // Issue a page break first if needed
        $this->CheckPageBreak($h);
        // Draw the cells of the row
        for($i=0;$i<count($data);$i++)
        {
            $w = $this->widths[$i];
            $a = isset($this->aligns[$i]) ? $this->aligns[$i] : 'L';
            // Save the current position
            $x = $this->GetX();
            $y = $this->GetY();
            // COlor de fondo de la celda
            $colorR = isset($this->bgColor[0]) ? $this->bgColor[0] : 255;
            $colorG = isset($this->bgColor[0]) ? $this->bgColor[1] : 255;
            $colorB = isset($this->bgColor[0]) ? $this->bgColor[2] : 255;

            $this->SetFillColor($colorR, $colorG, $colorB);
            // Draw the border
            $this->Rect($x,$y,$w,$h,"F");

            if (str_contains($data[$i],":")) {
                $this->SetFont('Arial','B',11);
            }
            // Print the text
            $this->MultiCell($w,5,utf8_decode($data[$i]),0,$a);

            $this->SetTextColor(0,0,0);
            $this->SetFont('Arial','',11);
            
            // Put the position to the right of the cell
            $this->SetXY($x+$w,$y);
        }
        // Go to the next line
        $this->Ln($h);
    }

    function CheckPageBreak($h)
    {
        // If the height h would cause an overflow, add a new page immediately
        if($this->GetY()+$h>$this->PageBreakTrigger)
            $this->AddPage($this->CurOrientation);
            $y = $this->GetY()+1;
            $this->SetY($y);

    }

    function NbLines($w, $txt)
    {
        // Compute the number of lines a MultiCell of width w will take
        if(!isset($this->CurrentFont))
            $this->Error('No font has been set');
        $cw = $this->CurrentFont['cw'];
        if($w==0)
            $w = $this->w-$this->rMargin-$this->x;
        $wmax = ($w-2*$this->cMargin)*1000/$this->FontSize;
        $s = str_replace("\r",'',(string)$txt);
        $nb = strlen($s);
        if($nb>0 && $s[$nb-1]=="\n")
            $nb--;
        $sep = -1;
        $i = 0;
        $j = 0;
        $l = 0;
        $nl = 1;
        while($i<$nb)
        {
            $c = $s[$i];
            if($c=="\n")
            {
                $i++;
                $sep = -1;
                $j = $i;
                $l = 0;
                $nl++;
                continue;
            }
            if($c==' ')
                $sep = $i;
            $l += $cw[$c];
            if($l>$wmax)
            {
                if($sep==-1)
                {
                    if($i==$j)
                        $i++;
                }
                else
                    $i = $sep+1;
                $sep = -1;
                $j = $i;
                $l = 0;
                $nl++;
            }
            else
                $i++;
        }
        return $nl;
    }

    // PRUEBA
    function Sector($xc, $yc, $r, $a, $b, $style='FD', $cw=true, $o=90)
    {
        $d0 = $a - $b;
        if($cw){
            $d = $b;
            $b = $o - $a;
            $a = $o - $d;
        }else{
            $b += $o;
            $a += $o;
        }
        while($a<0)
            $a += 360;
        while($a>360)
            $a -= 360;
        while($b<0)
            $b += 360;
        while($b>360)
            $b -= 360;
        if ($a > $b)
            $b += 360;
        $b = $b/360*2*M_PI;
        $a = $a/360*2*M_PI;
        $d = $b - $a;
        if ($d == 0 && $d0 != 0)
            $d = 2*M_PI;
        $k = $this->k;
        $hp = $this->h;
        if (sin($d/2))
            $MyArc = 4/3*(1-cos($d/2))/sin($d/2)*$r;
        else
            $MyArc = 0;
        //first put the center
        $this->_out(sprintf('%.2F %.2F m',($xc)*$k,($hp-$yc)*$k));
        //put the first point
        $this->_out(sprintf('%.2F %.2F l',($xc+$r*cos($a))*$k,(($hp-($yc-$r*sin($a)))*$k)));
        //draw the arc
        if ($d < M_PI/2){
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
        }else{
            $b = $a + $d/4;
            $MyArc = 4/3*(1-cos($d/8))/sin($d/8)*$r;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
            $a = $b;
            $b = $a + $d/4;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
            $a = $b;
            $b = $a + $d/4;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
            $a = $b;
            $b = $a + $d/4;
            $this->_Arc($xc+$r*cos($a)+$MyArc*cos(M_PI/2+$a),
                        $yc-$r*sin($a)-$MyArc*sin(M_PI/2+$a),
                        $xc+$r*cos($b)+$MyArc*cos($b-M_PI/2),
                        $yc-$r*sin($b)-$MyArc*sin($b-M_PI/2),
                        $xc+$r*cos($b),
                        $yc-$r*sin($b)
                        );
        }
        //terminate drawing
        if($style=='F')
            $op='f';
        elseif($style=='FD' || $style=='DF')
            $op='b';
        else
            $op='s';
        $this->_out($op);
    }

    function _Arc($x1, $y1, $x2, $y2, $x3, $y3 )
    {
        $h = $this->h;
        $this->_out(sprintf('%.2F %.2F %.2F %.2F %.2F %.2F c',
            $x1*$this->k,
            ($h-$y1)*$this->k,
            $x2*$this->k,
            ($h-$y2)*$this->k,
            $x3*$this->k,
            ($h-$y3)*$this->k));
    }

    function SetDash($black=null, $white=null)
    {
        if($black!==null)
            $s=sprintf('[%.3F %.3F] 0 d',$black*$this->k,$white*$this->k);
        else
            $s='[] 0 d';
        $this->_out($s);
    }

    // TRANSPARECIA EN IMAGENES///////////////////////////////////////////////////////////////////////////////
    protected $extgstates = array();

	// alpha: real value from 0 (transparent) to 1 (opaque)
	// bm:    blend mode, one of the following:
	//          Normal, Multiply, Screen, Overlay, Darken, Lighten, ColorDodge, ColorBurn,
	//          HardLight, SoftLight, Difference, Exclusion, Hue, Saturation, Color, Luminosity
	function SetAlpha($alpha, $bm='Normal')
	{
		// set alpha for stroking (CA) and non-stroking (ca) operations
		$gs = $this->AddExtGState(array('ca'=>$alpha, 'CA'=>$alpha, 'BM'=>'/'.$bm));
		$this->SetExtGState($gs);
	}

	function AddExtGState($parms)
	{
		$n = count($this->extgstates)+1;
		$this->extgstates[$n]['parms'] = $parms;
		return $n;
	}

	function SetExtGState($gs)
	{
		$this->_out(sprintf('/GS%d gs', $gs));
	}

	function _enddoc()
	{
		if(!empty($this->extgstates) && $this->PDFVersion<'1.4')
			$this->PDFVersion='1.4';
		parent::_enddoc();
	}

	function _putextgstates()
	{
		for ($i = 1; $i <= count($this->extgstates); $i++)
		{
			$this->_newobj();
			$this->extgstates[$i]['n'] = $this->n;
			$this->_put('<</Type /ExtGState');
			$parms = $this->extgstates[$i]['parms'];
			$this->_put(sprintf('/ca %.3F', $parms['ca']));
			$this->_put(sprintf('/CA %.3F', $parms['CA']));
			$this->_put('/BM '.$parms['BM']);
			$this->_put('>>');
			$this->_put('endobj');
		}
	}

	function _putresourcedict()
	{
		parent::_putresourcedict();
		$this->_put('/ExtGState <<');
		foreach($this->extgstates as $k=>$extgstate)
			$this->_put('/GS'.$k.' '.$extgstate['n'].' 0 R');
		$this->_put('>>');
	}

	function _putresources()
	{
		$this->_putextgstates();
		parent::_putresources();
	}


    // IMprimir HTML ///////////////////////////////////////////////////////////////////////////
    var $B=0;
	var $I=0;
	var $U=0;
	var $HREF='';
	var $ALIGN='';

	function WriteHTML($html)
	{
		//HTML parser
		$html=str_replace("\n",' ',$html);
		$a=preg_split('/<(.*)>/U',$html,-1,PREG_SPLIT_DELIM_CAPTURE);
		foreach($a as $i=>$e)
		{
			if($i%2==0)
			{
				//Text
				if($this->HREF)
					$this->PutLink($this->HREF,$e);
				elseif($this->ALIGN=='center')
					$this->Cell(0,5,$e,1,1,'C');
				else
					$this->Write(5,$e);
			}
			else
			{
				//Tag
				if($e[0]=='/')
					$this->CloseTag(strtoupper(substr($e,1)));
				else
				{
					//Extract properties
					$a2=explode(' ',$e);
					$tag=strtoupper(array_shift($a2));
					$prop=array();
					foreach($a2 as $v)
					{
						if(preg_match('/([^=]*)=["\']?([^"\']*)/',$v,$a3))
							$prop[strtoupper($a3[1])]=$a3[2];
					}
					$this->OpenTag($tag,$prop);
				}
			}
		}
	}

	function OpenTag($tag,$prop)
	{
		//Opening tag
		if($tag=='B' || $tag=='I' || $tag=='U')
			$this->SetStyle($tag,true);
		if($tag=='A')
			$this->HREF=$prop['HREF'];
		if($tag=='BR')
			$this->Ln(5);
		if($tag=='P')
			$this->ALIGN=$prop['ALIGN'];
		if($tag=='HR')
		{
			if( !empty($prop['WIDTH']) )
				$Width = $prop['WIDTH'];
			else
				$Width = $this->w - $this->lMargin-$this->rMargin;
			$this->Ln(2);
			$x = $this->GetX();
			$y = $this->GetY();
			$this->SetLineWidth(0.4);
			$this->Line($x,$y,$x+$Width,$y);
			$this->SetLineWidth(0.2);
			$this->Ln(2);
		}
	}

	function CloseTag($tag)
	{
		//Closing tag
		if($tag=='B' || $tag=='I' || $tag=='U')
			$this->SetStyle($tag,false);
		if($tag=='A')
			$this->HREF='';
		if($tag=='P')
			$this->ALIGN='';
	}

	function SetStyle($tag,$enable)
	{
		//Modify style and select corresponding font
		$this->$tag+=($enable ? 1 : -1);
		$style='';
		foreach(array('B','I','U') as $s)
			if($this->$s>0)
				$style.=$s;
		$this->SetFont('',$style);
	}

	function PutLink($URL,$txt)
	{
		//Put a hyperlink
		$this->SetTextColor(0,0,255);
		$this->SetStyle('U',true);
		$this->Write(5,$txt,$URL);
		$this->SetStyle('U',false);
		$this->SetTextColor(0);
	}

    function cellMultiColor($stringParts) {
        $currentPointerPosition = 0;
        foreach ($stringParts as $part) {
            // Set the pointer to the end of the previous string part
            // $this->SetX($currentPointerPosition);
    
            // Get the color from the string part
            $this->SetTextColor($part['color'][0], $part['color'][1], $part['color'][2]);
            // Para negritas subrayado y crurciva va en la variable formato del array
            if(isset($part["formato"])){
                $this->SetFont('',$part["formato"]);
            }

            // $this->Cell(0, 5, $part['text'],1,0,"FJ");
            $this->Write(5,utf8_decode($part['text']),"","FJ");
            // $this->MultiCell(0,5,utf8_decode($part['text']),1,"J");
            
            // Quitamos el fromato negritas subryadato
            $this->SetFont('');
            // Regresamos el color del texto por defecto negro
            $this->SetTextColor(0,0,0);

            // Update the pointer to the end of the current string part
            $currentPointerPosition += $this->GetStringWidth($part['text']);
        }
        // $this->Ln();
    }

}